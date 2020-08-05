public class Deal {
    private int count;
    private int buySystem;
    private int sellSystem;
    private double buyPrice;
    private double sellPrice;
    private int type;

    public Deal() {
        type = 0;
        count = 0;
        buyPrice = 0;
        sellPrice = 0;
        buySystem = 0;
        sellSystem = 0;
    }

    public Deal(int type, int count, double buyPrice, double sellPrice, int buySystem, int sellSystem) {
        this.type = type;
        this.count = count;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.buySystem = buySystem;
        this.sellSystem = sellSystem;
    }

    public void add(int count) {
        this.count += count;
    }

    public double getBuyPrice() {
        return count * buyPrice;
    }

    public double getSellPrice() {
        return count * sellPrice;
    }

    public double getMargin() {
        return getSellPrice() - getBuyPrice();
    }

    public int getType() { return type; }

    public  int getBuySystem() { return buySystem; }

    public int getSellSystem() { return sellSystem; }

    public int getCount() { return count; }
}