package system;

import component.IMovementStrategy;
import component.ISkillStrategy;
import component.BirthSkill;
import component.ChaseMovement;
import component.DashSkill;
import component.FlightSkill;
import component.ShootSkill;

public class MonsterData {

	public static class MonsterInfo {
		// --- 1. 識別與 UI 圖鑑資料 (UI 系統專用，實體不存) ---
		public final int type;
		public final String name;
		public final String desc;
		public final String harm;

		// --- 2. 實體底層數值 (Spawner 洗白怪物時的基準值) ---
		public int baseHealth;
		public final float baseSpeed;
		public final int baseDamage;
		public final int rewardExp; // C 同學的掉落物系統需要的經驗值

		public final IMovementStrategy moveStrategy;
		public final ISkillStrategy skillStrategy;
		public final long skillCooldown; // 若無技能，可設為 0

		public MonsterInfo(int type, String name, String desc, String harm,
				int baseHealth, float baseSpeed, int baseDamage, int rewardExp,
				IMovementStrategy moveStrategy, ISkillStrategy skillStrategy, long skillCooldown) {
			this.type = type;
			this.name = name;
			this.desc = desc;
			this.harm = harm;
			this.baseHealth = baseHealth;
			this.baseDamage = baseDamage;
			this.baseSpeed = baseSpeed;
			this.rewardExp = rewardExp;
			this.moveStrategy = moveStrategy;
			this.skillStrategy = skillStrategy;
			this.skillCooldown = skillCooldown;
		}
	}

	public static final MonsterInfo[] MONSTERS = {
			new MonsterInfo(
					0, // 标签
					"塑膠袋幽靈",
					"隨風飄散的塑膠袋，容易被野生動物誤食",
					"塑膠分解需500年，海洋生物誤食後死亡", // 描述
					50, 2.5f, 10, 10, // 生命，速度，伤害，获得经验
					ChaseMovement.INSTANCE/* 正常追击 */, null, 0// 移动组件，技能组件，冷却

			),
			new MonsterInfo(
					1,
					"油污史萊姆",
					"黑色油污狀的史萊姆，快速擴散污染範圍",
					"油輪洩漏造成海洋生態浩劫，生物窒息死亡", // 描述
					70, 1.3f, 10, 15, // 生命，速度，伤害，获得经验
					ChaseMovement.INSTANCE, DashSkill.INSTANCE/* 每段时间后向玩家冲刺一段距离 */, 3000// 移动组件，技能组件，冷却
			),
			new MonsterInfo(
					2,
					"病媒蚊群",
					"積水垃圾滋生的蚊子群，飛在空中難以捕捉",
					"登革熱、茲卡病毒傳播，威脅公共衛生", // 描述
					90, 2.5f, 10, 20, // 生命，速度，伤害，获得经验
					ChaseMovement.INSTANCE, FlightSkill.INSTANCE/* 每段时间后起飞，时间内不受伤害 */, 6000// 移动组件，技能组件，冷却
			),
			new MonsterInfo(
					3,
					"菸鬼",
					"隨手丟棄菸蒂的怪物，朝玩家發射菸蒂",
					"菸蒂含重金屬及致癌物，污染土壤與水源", // 描述
					50, 1.5f, 5, 20, // 生命，速度，伤害，获得经验
					null, ShootSkill.INSTANCE/* 每段时间朝玩家方向发射菸蒂,造成些微伤害 */, 3000// 移动组件，技能组件，冷却
			),
			new MonsterInfo(
					4,
					"塑膠團怪",
					"一大團纏繞在一起的塑膠袋，會分裂出小塑膠袋",
					"太平洋垃圾帶面積相當於44個台灣，危害海洋生態", // 描述
					200, 1.0f, 8, 25, // 生命，速度，伤害，获得经验
					ChaseMovement.INSTANCE, BirthSkill.INSTANCE/* 每5秒额外生成一只塑膠袋幽靈，上限3 */, 5000// 移动组件，技能组件，冷却
			)
	};

	public static MonsterInfo getMonster(int type) {
		if (type < 0 || type >= MONSTERS.length) {
			System.err.println("請求了非法的怪物 Type [" + type + "]，生成失败！奖励你一只普通怪");
			return MONSTERS[0]; // 退回預設的安全藍圖
		}

		MonsterInfo info = MONSTERS[type];
		if (info == null) {
			System.err.println("怪物 Type [" + type + "] 資料為空，生成失败！奖励你一只普通怪");
			return MONSTERS[0];
		}
		return info;
	}

	public static int getMonsterCount() {
		return MONSTERS.length;
	}
}