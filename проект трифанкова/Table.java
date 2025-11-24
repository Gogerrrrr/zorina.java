public class Table extends GameObject {
    private boolean clean;
    private int tableNumber;

    public Table(int x, int y, int tableNumber) {
        super(x, y);
        this.clean = true;
        this.tableNumber = tableNumber;
    }

    public boolean isClean() { return clean; }
    public void setClean(boolean clean) { this.clean = clean; }
    public int getTableNumber() { return tableNumber; }
}