package arm;

import weka.associations.AbstractAssociator;
import weka.core.Instances;

import java.util.*;
import java.util.Map.Entry;

public class FPTree extends AbstractAssociator implements IFPTree {

    @Override
    public List<IRule> getAssociationRules() {
        var res = new ArrayList<IRule>();
        for (var entry : frequentMap.entrySet()) {

        }
        return res;
    }

    @Override
    public void buildAssociations(Instances instances) {
//        if (instances.classIndex() >= 0) instances.deleteAttributeAt(instances.classIndex());
//        instances.deleteAttributeAt(instances.numAttributes() - 1);
        List<List<String>> transactions = new ArrayList<>();
        var attributes = Collections.list(instances.enumerateAttributes());
        var iter = instances.enumerateInstances();
        while (iter.hasMoreElements()) {
            var transaction = new ArrayList<String>();
            var instance = iter.nextElement();
            attributes.forEach(attr -> {
                transaction.add(String.format("%s=%s", attr.name(), instance.stringValue(attr)));
            });
            transactions.add(transaction);
        }
        FPGrowthAlgorithm(transactions);
    }

    public void printResult(int minLength) {
        System.out.println(String.format("FP Growth found %d rules: ", frequentMap.size()));
        int count = 0;
        for (Entry<List<String>, Integer> entry : this.frequentMap.entrySet()) {
            List<String> rule = entry.getKey();
            if (rule.size() >= minLength) continue;
            Integer support = entry.getValue();
            System.out.println(String.format("%d. %s => %d", count++, rule, support));
        }
    }

    FPNode root;
    int min_sup = 100;
    private Map<List<String>, Integer> frequentMap = new HashMap<List<String>, Integer>();

    public void FPGrowthAlgorithm(List<List<String>> transactions) {
        //-- This is the first Data Scan
        HashMap<String, Integer> itemCount = getFreqCount(transactions);
        //Sort items according to itemCount
        for (List<String> transaction : transactions) {
            Collections.sort(transaction, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    // TODO Auto-generated method stub
                    if (itemCount.get(o1) > itemCount.get(o2)) return -1;
                    else if (itemCount.get(o1) < itemCount.get(o2)) return 1;
                    return 0;
                }
            });
        }

        //build tree
        FPGrowth(transactions, null);
    }

    public void FPGrowth(List<List<String>> transactions, List<String> postModel) {
        Map<String, Integer> itemCount = getFreqCount(transactions);
        Map<String, FPNode> headerTable = new HashMap<>();

        // set header table
        for (Entry<String, Integer> entry : itemCount.entrySet()) {
            String itemName = entry.getKey();
            Integer count = entry.getValue();

            //check the min_support
//            if (count >= this.min_sup) {
            FPNode node = new FPNode(itemName);
            node.support = count;
            headerTable.put(itemName, node);
//            }
        }

        FPNode root = buildTree(transactions, itemCount, headerTable);

        if (root == null) return;

        if (root.children == null || root.children.size() == 0) return;

        //optimization for single path
        if (isSingleBranch(root)) {
            ArrayList<FPNode> path = new ArrayList<>();
            FPNode curr = root;
            while (curr.children != null && curr.children.size() > 0) {
                String childName = curr.children.keySet().iterator().next();
                curr = curr.children.get(childName);
                path.add(curr);
            }

            List<List<FPNode>> combinations = new ArrayList<>();
            getCombinations(path, combinations);

            for (List<FPNode> combine : combinations) {
                int supp = 0;
                List<String> rule = new ArrayList<>();
                for (FPNode node : combine) {
                    rule.add(node.itemName);
                    supp = node.support;
                }
                if (postModel != null) {
                    rule.addAll(postModel);
                }

                frequentMap.put(rule, supp);
            }

            return;
        }

        for (FPNode header : headerTable.values()) {

            List<String> rule = new ArrayList<>();
            rule.add(header.itemName);// header is item >= min_support

            if (postModel != null) {
                rule.addAll(postModel);
            }

            frequentMap.put(rule, header.support);

            List<String> newPostPattern = new ArrayList<>();
            newPostPattern.add(header.itemName);
            if (postModel != null) {
                newPostPattern.addAll(postModel);
            }

            //new conditional pattern base
            List<List<String>> newCPB = new LinkedList<List<String>>();
            FPNode nextNode = header;
            while ((nextNode = nextNode.next) != null) {
                int leaf_supp = nextNode.support;

                //get the path from root to this node
                LinkedList<String> path = new LinkedList<>();
                FPNode parent = nextNode;
                while (!(parent = parent.parent).itemName.equals("ROOT")) {
                    path.push(parent.itemName);
                }
                if (path.size() == 0) continue;

                while (leaf_supp-- > 0) {
                    newCPB.add(path);
                }
            }
            FPGrowth(newCPB, newPostPattern);
        }
    }

    private void getCombinations(ArrayList<FPNode> path, List<List<FPNode>> combinations) {
        if (path == null || path.size() == 0) return;
        int length = path.size();
        for (int i = 1; i < Math.pow(2, length); i++) {
            String bitmap = Integer.toBinaryString(i);
            List<FPNode> combine = new ArrayList<>();
            for (int j = 0; j < bitmap.length(); j++) {
                if (bitmap.charAt(j) == '1') {
                    combine.add(path.get(length - bitmap.length() + j));
                }
            }
            combinations.add(combine);
        }
    }


    private FPNode buildTree(List<List<String>> transactions, final Map<String, Integer> itemCount, final Map<String, FPNode> headerTable) {
        FPNode root = new FPNode("ROOT");
        root.parent = null;

        for (List<String> transaction : transactions) {
            FPNode prev = root;
            HashMap<String, FPNode> children = prev.children;

            for (String itemName : transaction) {
                //not in headerTable, then not qualify the min support.
                if (!headerTable.containsKey(itemName)) continue;

                FPNode t;
                if (children.containsKey(itemName)) {
                    children.get(itemName).support++;
                    t = children.get(itemName);
                } else {
                    t = new FPNode(itemName);
                    t.parent = prev;
                    children.put(itemName, t);

                    //add to header
                    FPNode header = headerTable.get(itemName);
                    if (header != null) {
                        header.attach(t);
                    }
                }
                prev = t;
                children = t.children;
            }
        }

        return root;

    }

    private boolean isSingleBranch(FPNode root) {
        boolean rect = true;
        while (root.children != null && root.children.size() > 0) {
            if (root.children.size() > 1) {
                rect = false;
                break;
            }
            String childName = root.children.keySet().iterator().next();
            root = root.children.get(childName);
        }
        return rect;
    }

    private HashMap<String, Integer> getFreqCount(List<List<String>> transactions) {
        HashMap<String, Integer> itemCount = new HashMap<String, Integer>();
        for (List<String> transac : transactions) {
            for (String item : transac) {
                if (itemCount.containsKey(item)) {
                    int count = itemCount.get(item);
                    itemCount.put(item, ++count);
                } else {
                    itemCount.put(item, 1);
                }
            }
        }

        return itemCount;
    }

    class FPNode {

        int support;
        String itemName;
        HashMap<String, FPNode> children;
        FPNode next; //use for header table
        FPNode parent;

        public FPNode(String name) {
            this.itemName = name;
            this.support = 1;
            this.children = new HashMap<String, FPNode>();
            this.next = null;
            this.parent = null;
        }

        @Override
        public String toString() {
            return "FPNode [support=" + support + ", itemName=" + itemName + "]";
        }

        public void attach(FPNode t) {
            FPNode node = this;
            while (node.next != null) {
                node = node.next;
            }
            node.next = t;
        }
    }

    class FPRule implements IRule {
        List<String> pattern;
        int frequency;

        public FPRule(List<String> pattern, int frequency) {
            this.pattern = pattern;
            this.frequency = frequency;
        }

        @Override
        public List<IConditionalItem> getPremise() {
            List<IConditionalItem> res = new ArrayList<>();
            for (var p : pattern) {
                var tmp = p.split("=");
                var a = tmp[0];
                var b = "";
                if (tmp.length > 1) b = tmp[1];
                res.add(new FPConditionalItem(a, b));
            }
            return res;
        }

        @Override
        public List<IConditionalItem> getConsequences() {
            return Arrays.asList(new FPConditionalItem("frequency", String.valueOf(frequency)));
        }
    }

    class FPConditionalItem implements IConditionalItem {
        String name;
        String value;

        public FPConditionalItem(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getItemName() {
            return this.name;
        }

        @Override
        public String getItemValue() {
            return this.value;
        }
    }
}
