package se.cygni.snake;

public class BST {
    public static Node root;

    public BST() {
        this.root = null;
    }
/*
 * AVL_Tree.tcc    (c) Tommy Olsson, IDA, 2007-05-02
 * updated to C++11 September 2015
 */

    /*
     * AVL_Tree_Node.
     */
    class Node {
        int element;
        int idx;
        Node left;
        Node right;
        int height;

        public Node(int data, int n) {
            this.element = data;
            this.idx = n;
            left = null;
            right = null;
        }
    }

    ;

    public int max(int left, int right) {
        return (left >= right) ? left : right;
    }


/*=============================================*
 *                                             *
 *                AVL_Tree_Node                *
 *                                             *
 *=============================================*/

/*
 * AVL rotations and corrsponding help functions.
 */

    /**
     * Return the height of a node (sub tree).
     */
    public int node_height(Node p) {
        return (p != null ? p.height : -1);
    }

    /**
     * Adjust the height for node p.
     */
    public void calculate_height(Node p) {
        p.height = 1 + max(node_height(p.left), node_height(p.right));
    }

    /**
     * Single rotation left-to-right using node k2 as pivot.
     */
    public void single_rotate_with_left_child(Node k2) {
        Node k1 = k2.left;

        k2.left = k1.right;
        k1.right = k2;

        k2.height = max(node_height(k2.left), node_height(k2.right)) + 1;
        k1.height = max(node_height(k1.left), k2.height) + 1;

        k2 = k1;
    }

    /**
     * Single rotation right-to-left using node k1 as pivot.
     */
    public void single_rotate_with_right_child(Node&k1) {
        Node k2 = k1.right;

        k1.right = k2.left;
        k2.left = k1;

        k1.height = max(node_height(k1.right), node_height(k1.left)) + 1;
        k2.height = max(node_height(k2.right), k1.height) + 1;

        k1 = k2;
    }

    /**
     * Double rotation left-to-right using node k3 as pivot.
     */
    template<typename Comparable>
    void
    AVL_Tree_Node<Comparable>::

    double_rotate_with_left_child(Node&k3) {
        single_rotate_with_right_child(k3.left);
        single_rotate_with_left_child(k3);
    }

    /**
     * Double rotation right-to-left using node k3 as pivot.
     */
    template<typename Comparable>
    void
    AVL_Tree_Node<Comparable>::

    double_rotate_with_right_child(Node&k3) {
        single_rotate_with_left_child(k3.right);
        single_rotate_with_right_child(k3);
    }

/*
 * Member functions for AVL_Tree_Node
 */

    /**
     * Insert x in tree t as a new leaf. Check balance and adjust tree if needed.
     */
    template<typename Comparable>
    void
    AVL_Tree_Node<Comparable>::

    insert(const Comparable&x, Node&t) {
        if (t == null) {
            t = new Node(x);
            return;
        }

        if (x < t.element) {
            insert(x, t.left);

            if (node_height(t.left) - node_height(t.right) == 2)
                if (x < t.left.element)
                    single_rotate_with_left_child(t);
                else
                    double_rotate_with_left_child(t);
            else
                calculate_height(t);
        } else if (t.element < x) {
            insert(x, t.right);

            if (node_height(t.right) - node_height(t.left) == 2)
                if (t.right.element < x)
                    single_rotate_with_right_child(t);
                else
                    double_rotate_with_right_child(t);
            else
                calculate_height(t);
        } else {
            throw AVL_Tree_error("insättning: finns redan");
        }
    }

    /**
     * Remove x in tree t. Check balance and adjust tree if needed.
     */
    template<typename Comparable>
    void
    AVL_Tree_Node<Comparable>::

    remove(const Comparable&x, Node&t) {
        if (t == null) {
            //return;  // Här kan ett undantag genereras i stället ...
            throw AVL_Tree_error("borttagning: värdet att ta bort hittades inte!");
        }

        if (x < t.element) {
            remove(x, t.left);

            if (node_height(t.right) - node_height(t.left) == 2) {
                if (node_height(t.right.right) > node_height(t.right.left))
                    single_rotate_with_right_child(t);
                else
                    double_rotate_with_right_child(t);
            } else
                calculate_height(t);

        } else if (t.element < x) {
            remove(x, t.right);

            if (node_height(t.left) - node_height(t.right) == 2) {
                if (node_height(t.left.left) > node_height(t.left.right))
                    single_rotate_with_left_child(t);
                else
                    double_rotate_with_left_child(t);
            } else
                calculate_height(t);

        } else {

            // Sökt värde finns i noden t
            Node tmp;

            if (t.left != null && t.right != null) {
                // Noden har två barn och ersätts med inorder efterföljare
                tmp = find_min(t.right);
                t.element = tmp.element;
                remove(t.element, t.right);

                if (node_height(t.left) - node_height(t.right) == 2) {
                    if (node_height(t.left.left) > node_height(t.left.right))
                        single_rotate_with_left_child(t);
                    else
                        double_rotate_with_left_child(t);
                } else
                    calculate_height(t);


            } else {
                // Noden har inget eller ett barn
                tmp = t;

                if (t.left == null)
                    t = t.right;
                else
                    t = t.left;

                delete tmp;
            }
        }

    }


    /**
     * Look for x in the tree. If found, return av pointer to the node.
     * Otherwise, return null.
     */
    public Node find(const Comparable&x, const Node t) {
        if (t == null)
            return null;
        else if (x < t.element)
            return find(x, t.left);
        else if (t.element < x)
            return find(x, t.right);
        else
            return t;
    }

    /**
     * Look for the smallest value (the leftmost node). Return pointer to that node.
     * In case of empty tree, return null.
     */
    template<typename Comparable>
    AVL_Tree_Node<Comparable>*
    AVL_Tree_Node<Comparable>::

    find_min(const Node t) {
        if (t == null)
            return null;
        else if (t.left == null)
            return t;
        else
            return find_min(t.left);
    }

    /**
     * Look for the biggest value (the rightmost node). Return pointer to that node.
     * In case of empty tree, return null.
     */
    template<typename Comparable>
    AVL_Tree_Node<Comparable>*
    AVL_Tree_Node<Comparable>::

    find_max(const Node t) {
        Node p = t;
        if (p != null) {
            while (p.right != null)
                p = p.right;
        }
        return p;
    }

    /**
     * Remove all nodes in the tree.
     */
    template<typename Comparable>
    void
    AVL_Tree_Node<Comparable>::

    clear(Node&t) {
        if (t != null) {
            clear(t.left);
            clear(t.right);
            delete t;
            t = null;
        }
    }

    /**
     * Make indent matching the current tree depth.
     */
    template<typename Comparable>
    void
    AVL_Tree_Node<Comparable>::

    indent(ostream&os, int level) {
        for (int i = 0; i < level; ++i)
            os << "  ";
    }



/*=============================================*
 *                                             *
 *                  AVL_Tree                   *
 *                                             *
 *=============================================*/

    /**
     * Insert x in the tree.
     */
    template<typename Comparable>
    void
    AVL_Tree<Comparable>::

    insert(const Comparable&x) {
        Node::insert (x, root);
    }

    /**
     * Remove x from the tree.
     */
    template<typename Comparable>
    void
    AVL_Tree<Comparable>::

    remove(const Comparable&x) {
        //throw AVL_Tree_error("remove: ska implementeras!");
        Node::remove (x, root);
    }

    /**
     * Check if tree contains x.
     */
    template<typename Comparable>
    bool
    AVL_Tree<Comparable>::

    member(const Comparable&x) const

    {
        return Node::find (x, root) !=null;
    }


    /**
     * Look up value x in tree, return reference to that element.
     */
    public Node find(Node x)
    {
        Node tmp = find (x, root);

        if (tmp == null)
            throw AVL_Tree_error("sökt värde finns ej i trädet");

        return tmp.element;
    }

    /**
     * Look up the smallest value in the tree, return reference to it.
     */
    template<typename Comparable>
    Comparable&
    AVL_Tree<Comparable>::

    find_min() const

    {
        if (root == null)
            throw AVL_Tree_error("försök att finna minst i tomt träd");

        return Node::find_min (root).element;
    }

    /**
     * Look up the largest value in the tree, return reference to it.
     */
    public Node find_max()

    {
        if (root == null)
            throw AVL_Tree_error("försök att finna störst i tomt träd");

        return find_max (root).element;
    }

    /**
     * Check if tree is empty.
     */
    public boolean empty() {
        return root == null;
    }

    /**
     * Empty the tree.
     */
    public void clear() {
        clear(root);
    }


}


