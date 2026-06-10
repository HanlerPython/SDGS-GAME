package system;

public class SDGStats {
    
    private int totalRecycled;
    private int plasticRecycled;
    private int paperRecycled;
    private int glassRecycled;
    private int foodRecycled;
    
    public SDGStats() {
        this.totalRecycled = 0;
        this.plasticRecycled = 0;
        this.paperRecycled = 0;
        this.glassRecycled = 0;
        this.foodRecycled = 0;
    }
    
    public void addRecycled(int trashType) {
        totalRecycled++;
        switch (trashType) {
            case 1: plasticRecycled++; break;
            case 2: paperRecycled++; break;
            case 3: glassRecycled++; break;
            case 4: foodRecycled++; break;
        }
    }
    
    public int getCarbonSaved() {
        return (int)(totalRecycled * 0.1);
    }
    
    public int getTotalRecycled() { return totalRecycled; }
    public int getPlasticRecycled() { return plasticRecycled; }
    public int getPaperRecycled() { return paperRecycled; }
    public int getGlassRecycled() { return glassRecycled; }
    public int getFoodRecycled() { return foodRecycled; }
    
    public void reset() {
        totalRecycled = 0;
        plasticRecycled = 0;
        paperRecycled = 0;
        glassRecycled = 0;
        foodRecycled = 0;
    }
}