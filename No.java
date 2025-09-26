public class No implements Comparable<No> {
    char character;
    int frequencie;
    No left, right;

    public No (char character, int frequencie){
        this.character = character;
        this.frequencie = frequencie;
        this.left = null;
        this.right = null;
    }

    @Override
    public int compareTo(No outroNo) {
        return this.frequencie - outroNo.frequencie;
    }
}
