package component;

import entity.Monster;

public interface IMovementStrategy {
	void move(Monster entity,float targetX,float targetY);
}
