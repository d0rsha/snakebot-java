package se.cygni.snake;

public class BinarySearchTree {
        public static  Node root;
        public static int size;
        public static int head;
        public static int tail;
        public static int MAX_SIZE;
        public BinarySearchTree(){
            this.root = null;
            this.size = 0;
            this.head = 0;
            this.tail = 0;
            this.MAX_SIZE = 1000;
        }

        public boolean find(int id){
            Node current = root;
            while(current!=null){
                if(current.data==id){
                    return true;
                }else if(current.data>id){
                    current = current.left;
                }else{
                    current = current.right;
                }
            }
            return false;
        }
        public boolean delete(int id){
            Node parent = root;
            Node current = root;
            boolean isLeftChild = false;
            while(current.data!=id){
                parent = current;
                if(current.data>id){
                    isLeftChild = true;
                    current = current.left;
                }else{
                    isLeftChild = false;
                    current = current.right;
                }
                if(current ==null){
                    return false;
                }
            }
            //if i am here that means we have found the node
            //Case 1: if node to be deleted has no children
            if(current.left==null && current.right==null){
                if(current==root){
                    root = null;
                }
                if(isLeftChild ==true){
                    parent.left = null;
                }else{
                    parent.right = null;
                }
            }
            //Case 2 : if node to be deleted has only one child
            else if(current.right==null){
                if(current==root){
                    root = current.left;
                }else if(isLeftChild){
                    parent.left = current.left;
                }else{
                    parent.right = current.left;
                }
            }
            else if(current.left==null){
                if(current==root){
                    root = current.right;
                }else if(isLeftChild){
                    parent.left = current.right;
                }else{
                    parent.right = current.right;
                }
            }else if(current.left!=null && current.right!=null){

                //now we have found the minimum element in the right sub tree
                Node successor	 = getSuccessor(current);
                if(current==root){
                    root = successor;
                }else if(isLeftChild){
                    parent.left = successor;
                }else{
                    parent.right = successor;
                }
                successor.left = current.left;
            }
            size--;
            return true;
        }

        public Node getSuccessor(Node deleleNode){
            Node successsor =null;
            Node successsorParent =null;
            Node current = deleleNode.right;
            while(current!=null){
                successsorParent = successsor;
                successsor = current;
                current = current.left;
            }
            //check if successor has the right child, it cannot have left child for sure
            // if it does have the right child, add it to the left of successorParent.
//		successsorParent
            if(successsor!=deleleNode.right){
                successsorParent.left = successsor.right;
                successsor.right = deleleNode.right;
            }
            return successsor;
        }
        public void insert(int id){
            tail = (tail % MAX_SIZE) +1;
            Node newNode = new Node(id,tail);
            if(root==null){
                root = newNode;
                return;
            }
            Node current = root;
            Node parent = null;
            while(true){
                parent = current;
                if(id<current.data){
                    current = current.left;
                    if(current==null){
                        parent.left = newNode;
                        size++;
                        return;
                    }
                }else if (id>current.data){
                    current = current.right;
                    if(current==null){
                        parent.right = newNode;
                        size++;
                        return;
                    }
                }
                else{// Insättning finns redan

                }
            }
        }
        public void display(Node root){
            if(root!=null){
                display(root.left);
                System.out.print(" " + root.data);
                display(root.right);
            }
        }

        private int find_head(Node root){
            if ( root == null )
                return 0;
            if ( root.idx == head )
                return head;
            int found = 0;
            if (root.left != null){
                found = find_head(root.left);
            }
            if ( found != 0 ){
                return found;
            }
            else if (root.right != null){
                found = find_head(root.right);
            }
            if ( found != 0 ){
                return found;
            }
            return found;
        }

        public int get_head(){
            if (root == null){
                return -1;
            }
            return find_head(root);
            /*
            if (root.idx == head){
                return root.data;
            }
            int fail_if_zero = find_head(root.left);
            if(fail_if_zero != 0){
                delete((fail_if_zero));
                head = (head  % MAX_SIZE) +1;
                return fail_if_zero;
            }
            fail_if_zero = find_head(root.right);
            if (fail_if_zero != 0){
                delete(fail_if_zero);
                head = (head  % MAX_SIZE) +1;
                return fail_if_zero;
            }
            //Failed to find value
            return -1;
*/
        }
    // public static void main(String arg[]){}


    class Node{
        int data;
        int idx;
        Node left;
        Node right;
        public Node(int data, int n){
            this.data = data;
            this.idx = n;
            left = null;
            right = null;
        }
    }
}
