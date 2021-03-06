package com.tks.vertshoo.bullet;

import com.tks.vertshoo.R;
import com.tks.vertshoo.fighter.FighterBase;
import com.tks.vertshoo.scene.GameSceneBase;

/**
 * 敵を狙撃する弾
 * @author TAKESHI YAMASHITA
 *
 */
public class SnipeBullet extends DirectionBullet {

    public SnipeBullet(GameSceneBase scene, FighterBase shooter) {
        super(scene, shooter);
        // 効果音を再生
        scene.playSE(R.raw.enemy_bullet);
    }

    /**
     * 特定の敵を狙わせる
     * @param target
     * @param speed
     */
    public void setup(FighterBase target, float speed) {
        offset.x = target.getPositionX() - getPositionX();
        offset.y = target.getPositionY() - getPositionY();
        offset.normalize();
        offset.mul(speed);
    }
}