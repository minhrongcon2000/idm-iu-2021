package arm.fpgrowth;

import arm.IFPTree;
import arm.IRule;
import weka.associations.AbstractAssociator;
import weka.core.Instances;

import java.util.*;
import java.util.Map.Entry;

public class FPTree extends AbstractAssociator implements IFPTree {
    public FPTree() {
        this(new FPOptions());
    }

    public FPTree(FPOptions options) {
        this.options = options;
    }

    @Override
    public List<IRule> getAssociationRules() {
        var rules = new ArrayList<IRule>();
        getFrequentItemSets().forEach((itemSet, support) -> {
            if (itemSet.size() >= options.getMinItemSetLen()) {
                var subsets = generateAllSubsets(itemSet);
                subsets.removeIf(Set::isEmpty);
                subsets.removeIf(set -> set.containsAll(itemSet));
                subsets.forEach(ss -> {
                    var confidence = (double) support / (double) supportCount(ss);
                    if (confidence >= options.getMinConfidence()) {
                        var diffSet = setDifference(itemSet, ss);
                        rules.add(new FPRule(ss, supportCount(ss), diffSet, supportCount(diffSet), confidence));
                    }
                });
            }
        });
        return rules;
    }

    @Override
    public void buildAssociations(Instances instances) {
//        if (instances.classIndex() >= 0) instances.deleteAttributeAt(instances.classIndex());
//        instances.deleteAttributeAt(instances.numAttributes() - 1);
        var attributes = Collections.list(instances.enumerateAttributes());
        var iter = instances.enumerateInstances();
        while (iter.hasMoreElements()) {
            var transaction = new ArrayList<String>();
            var instance = iter.nextElement();
            attributes.forEach(attr -> {
                var val = instance.stringValue(attr);
                if (val.equalsIgnoreCase(options.getPositiveLabel()))
                    transaction.add(String.format("%s=%s", attr.name(), val));
            });
            transactions.add(transaction);
        }
        FPGrowthAlgorithm(transactions);
    }

    public Map<Set<String>, Integer> getFrequentItemSets() {
        var res = new HashMap<Set<String>, Integer>();
        for (var entry : this.frequentMap.entrySet()) {
            if (entry.getValue() > options.getMinItemSetSupport()) {
                res.put(entry.getKey(), entry.getValue());
            }
        }
        return res;
    }

    public void printFrequentItemSets() {
        System.out.printf("FP Growth found %d item sets: %n", frequentMap.size());
        int count = 0;
        for (var entry : this.frequentMap.entrySet()) {
            Set<String> rule = entry.getKey();
            if (rule.size() < options.getMinItemSetLen()) continue;
            Integer support = entry.getValue();
            System.out.printf("%d. %s => %d%n", count++, rule, support);
        }
    }

    FPNode root;
    FPOptions options;
    List<List<String>> transactions = new ArrayList<>();
    Map<Set<String>, Integer> frequentMap = new HashMap<Set<String>, Integer>();

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

//            check the min_support
            if (count >= options.getMinSup()) {
                FPNode node = new FPNode(itemName);
                node.support = count;
                headerTable.put(itemName, node);
            }
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
                Set<String> rule = new HashSet<>();
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

            Set<String> rule = new HashSet<>();
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

    <T> Set<Set<T>> generateAllSubsets(Set<T> original) {
        Set<Set<T>> allSubsets = new HashSet<Set<T>>();

        allSubsets.add(new HashSet<T>()); //Add empty set.

        for (T element : original) {
            // Copy subsets so we can iterate over them without ConcurrentModificationException
            Set<Set<T>> tempClone = new HashSet<Set<T>>(allSubsets);

            // All element to all subsets of the current power set.
            for (Set<T> subset : tempClone) {
                Set<T> extended = new HashSet<T>(subset);
                extended.add(element);
                allSubsets.add(extended);
            }
        }

        return allSubsets;
    }

    <T> Set<T> setDifference(Set<T> minuend, Set<T> substrahend) {
        var diffSet = new HashSet<T>(minuend);
        diffSet.removeAll(substrahend);
        return diffSet;
    }

    int supportCount(Set<String> target) {
        int count = 0;
        for (var transaction : transactions) {
            var items = new HashSet<>(transaction);
            if (items.containsAll(target)) count++;
        }
        return count;
    }
}
