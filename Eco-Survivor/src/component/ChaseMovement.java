package component;

import entity.Monster;
public class ChaseMovement implements IMovementStrategy{
	public static final ChaseMovement INSTANCE = new ChaseMovement(); 
    private ChaseMovement() {}
	@Override
    public void move(Monster entity, float targetX, float targetY) {
        double dx = targetX - entity.getX();
        double dy = targetY - entity.getY();
        double len = Math.hypot(dx, dy);
        
        if (len > 0) {
            // 利用 entity 提供的 Getter/Setter 更新座標
            entity.setX(entity.getX() + (float)(dx / len) * entity.getSpeed());
            entity.setY(entity.getY() + (float)(dy / len) * entity.getSpeed());
        }
    }
}
