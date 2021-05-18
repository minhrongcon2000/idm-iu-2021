package arm;

import java.util.HashMap;

public class FPNode {
        int support;
        String itemName;
        HashMap<String, FPNode> children;
        FPNode next; //use for header table
        FPNode parent;

        public FPNode(String name) {
            this.itemName = name;
            this.support = 1;
            this.children = new HashMap<>();
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
