package component;

import entity.Monster;

public interface ISkillStrategy {
	boolean executeSkill(Monster self, float targetX, float targetY);
}
