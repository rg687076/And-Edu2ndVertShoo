package com.tks.vertshoo.scene;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.tks.vertshoo.Define;
import com.tks.vertshoo.FoxOne;
import com.tks.vertshoo.R;
import com.tks.vertshoo.bg.ScrollBackground;
import com.tks.vertshoo.bullet.BulletBase;
import com.tks.vertshoo.effect.EffectBase;
import com.tks.vertshoo.fighter.enemy.EnemyFighterBase;
import com.tks.vertshoo.fighter.enemy.EnemyFighterBase.MoveType;
import com.tks.vertshoo.fighter.enemy.Frisbee;
import com.tks.vertshoo.fighter.enemy.Frisbee.AttackType;
import com.tks.vertshoo.fighter.enemy.MotherShip;
import com.tks.vertshoo.fighter.enemy.Tongari;
import com.tks.vertshoo.input.AttackButton;
import com.tks.vertshoo.ui.BombsInfo;
import com.tks.vertshoo.ui.HPBar;
import com.eaglesakura.lib.android.game.graphics.Color;
import com.eaglesakura.lib.android.game.scene.SceneBase;
import com.eaglesakura.lib.android.game.scene.SceneManager;

public class GameSceneStage1 extends PlaySceneBase {

    /**
     * 背景素材
     */
    ScrollBackground background = null;

    /**
     * ボム発射ボタン
     */
    AttackButton bombButton = null;

    /**
     * HPバー
     */
    HPBar hpBar = null;

    /**
     * ボム残弾
     */
    BombsInfo bomsInfo = null;

    /**
     * 強制ゲームクリアフラグ
     */
    boolean gameClear = false;

    public GameSceneStage1(FoxOne game) {
        super(game);
    }

    /**
     * 敵の生成情報を管理するクラス
     * @author TAKESHI YAMASHITA
     *
     */
    class StageEnemyData {
        /**
        * 出現フレーム
        */
        int createFrame;

        /**
        * 出現させる敵タイプ
        */
        EnemyType enemyType;

        /**
        * 移動タイプ
        */
        MoveType moveType;

        /**
        * 出現X位置
        */
        float createX;

        /**
        * 出現Y位置
        */
        float createY;

        /**
        * 初期値を設定して敵情報を作成する
        * @param createFrame
        * @param enemyType
        * @param moveType
        * @param createX
        * @param createY
        */
        public StageEnemyData(int createFrame, EnemyType enemyType, MoveType moveType, float createX, float createY) {
            this.createFrame = createFrame;
            this.enemyType = enemyType;
            this.moveType = moveType;
            this.createX = createX;
            this.createY = createY;
        }

        /**
        * 指定フレームに達していたら敵を作成し、trueを返す。
        * @return
        */
        public boolean create() {
            if (frameCount < createFrame) {
                return false;
            }

            // 敵を作成する
            addEnemy(enemyType, moveType, createX, createY);

            return true;
        }
    }

    List<StageEnemyData> stageEnemyDataList = new ArrayList<GameSceneStage1.StageEnemyData>();

    /**
     * 配置情報を最初に登録する
     */
    @Override
    protected void initializeEnemy() { // 出現させるY座標
        final float CREATE_Y = -150;
        // プレイエリアの左右から幅を取得する
        final int PLAY_AREA_WIDTH = Define.PLAY_AREA_RIGHT - Define.PLAY_AREA_LEFT;
        try {
            InputStream is = game.getContext().getResources().openRawResource(R.raw.stage1);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = null;

            int createFrame = 0;
            // テキストファイルから全行読み込む
            while ((line = reader.readLine()) != null) {
                String[] chips = line.split(","); // カンマで区切る
                // インベーダーの時のように、区切りを行う

                // 1ラインの横方向をfor文で見る
                for (int index = 0; index < chips.length; ++index) {
                    // 配置対象の敵をテーブルから取り出す
                    int chipNumber;

                    try {
                        chipNumber = Integer.parseInt(chips[index]);
                    } catch (Exception e) {
                        // 読み込めないチップは255番として扱う
                        chipNumber = 255;
                    }

                    // 出現座標X
                    int createX = 0;
                    {
                        // プレイエリアを等分して、現在のindexに割り当てる
                        createX = PLAY_AREA_WIDTH / chips.length * index;
                        createX += PLAY_AREA_WIDTH / chips.length / 2;
                        createX += Define.PLAY_AREA_LEFT;
                    }

                    // 敵のタイプ
                    EnemyType enemyType = null;

                    // 敵の移動方法
                    MoveType moveType = null;

                    // 選択されているチップのX座標
                    int chipX = (chipNumber % 16);

                    // 選択されているチップのY座標
                    int chipY = (chipNumber / 16);

                    // チップのY座標で敵の動きが決まる
                    switch (chipY) {
                        case 0:
                            moveType = MoveType.Straight;
                            break;
                        case 1:
                            moveType = MoveType.Curved;
                            break;
                    }

                    // チップのX座標で敵の種類が決まる
                    switch (chipX) {
                        case 0:
                            enemyType = EnemyType.FrisbeeNotAttack;
                            break;
                        case 1:
                            enemyType = EnemyType.FrisbeeStraightAttack;
                            break;
                        case 2:
                            enemyType = EnemyType.FrisbeeSnipeAttack;
                            break;
                        case 3:
                            enemyType = EnemyType.TongariAllDirection;
                            break;
                        case 4:
                            enemyType = EnemyType.TongariLaser;
                            break;
                        case 5:
                            enemyType = EnemyType.TongariLaserAndDirection;
                            break;
                        case 6:
                            enemyType = EnemyType.BossMotherShip;
                            // bossは特殊なためNOTのみ
                            moveType = MoveType.Not;
                            break;
                    }

                    // 条件が揃ってれば登録
                    if (enemyType != null && moveType != null) {
                        stageEnemyDataList.add(new StageEnemyData(createFrame, enemyType, moveType, createX, CREATE_Y));
                    }

                }
                // 1ラインにつき30フレーム後に生成する
                createFrame += 30;
            }

            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("file load error!!");
        }
    }

    @Override
    public void onSceneStart(SceneManager manager, SceneBase before) {
        super.onSceneStart(manager, before);

        // ボムボタンを生成して、ボム用に初期化する
        bombButton = new AttackButton(this);
        bombButton.initBombButton();
        player.setBombButton(bombButton);

        // 背景素材を読み込む
        background = new ScrollBackground(game, new int[] {
                R.drawable.bg_0, R.drawable.bg_1, R.drawable.bg_2
        });

        // HPバーを生成する
        hpBar = new HPBar(this);

        // ボムの残弾数
        bomsInfo = new BombsInfo(this);
    }

    /**
     * 敵の種類を示すenum
     *
     */
    protected enum EnemyType {
        /**
         * 何もしないフリスビー
         */
        FrisbeeNotAttack,

        /**
         * 直線攻撃フリスビー
         */
        FrisbeeStraightAttack,

        /**
         * 狙撃フリスビー
         */
        FrisbeeSnipeAttack,
        /**
         * 全方位攻撃トンガリ
         */
        TongariAllDirection,

        /**
         * レーザー攻撃トンガリ
         */
        TongariLaser,

        /**
         * 全方位攻撃＋レーザートンガリ
         */
        TongariLaserAndDirection,

        /**
         * 敵母艦
         */
        BossMotherShip,
    }

    protected void addEnemy(EnemyType type, MoveType moveType, float x, float y) {
        EnemyFighterBase enemy = null;
        switch (type) {

            case FrisbeeNotAttack:
                enemy = new Frisbee(AttackType.Not, this);
                break;
            case FrisbeeStraightAttack:
                enemy = new Frisbee(AttackType.ShotStraight, this);
                break;
            case FrisbeeSnipeAttack:
                enemy = new Frisbee(AttackType.Snipe, this);
                break;

            case TongariAllDirection:
                enemy = new Tongari(Tongari.AttackType.AllDirection, this);
                break;
            case TongariLaser:
                enemy = new Tongari(Tongari.AttackType.Laser, this);
                break;
            case TongariLaserAndDirection:
                enemy = new Tongari(Tongari.AttackType.LaserAndDirection, this);
                break;
            case BossMotherShip:
                enemy = new MotherShip(this);
                break;
        }
        enemy.setPosition(x, y);

        // 移動タイプと敵の種類で呼び出しメソッドを変更する
        {
            switch (moveType) {
                case Straight:
                    if (type.toString().startsWith("Tongari")) {
                        // "Tongari"系列なら移動速度を3にする
                        enemy.initMoveStraight(3.0f);
                    } else if (type.toString().startsWith("Frisbee")) {
                        // "Frisbee"系列
                        enemy.initMoveStraight(5.0f);
                    }
                    break;

                case Curved:
                    if (type.toString().startsWith("Tongari")) {
                        // "Tongari"系列
                        enemy.initMoveCurve(45.0f, 3.0f, 0.05f);
                    } else if (type.toString().startsWith("Frisbee")) {
                        // "Frisbee"系列なら移動速度を5にする
                        enemy.initMoveCurve(70.0f, 3.0f, 0.2f);
                    }
                    break;

                default:
                    break;
            }
        }
        enemies.add(enemy);
    }

    /**
    * 敵の侵攻状態を更新する
    */
    protected void updateEnemyInvasion() {

        Iterator<StageEnemyData> iterator = stageEnemyDataList.iterator();
        // 全データをチェックする
        while (iterator.hasNext()) {
            StageEnemyData stageEnemyData = iterator.next();

            // 生成に成功したら、リストから排除する
            if (stageEnemyData.create()) {
                iterator.remove();
            }
        }
    }

    @Override
    public void onFrameBegin(SceneManager manager) {
        super.onFrameBegin(manager);

        // ボムボタンを更新する
        bombButton.update();

        // 敵の侵略状態を更新する
        updateEnemyInvasion();

        background.scroll(10);
    }

    @Override
    public void onFrameDraw(SceneManager manager) {
        // 背景を描画する
        {
            background.draw();
        }

        // プレイヤーが死んでいなければ描画処理を行う
        if (!player.isDead()) {
            player.draw(); // プレイヤーを描画する
        }

        // 敵を全て描画する
        for (EnemyFighterBase enemy : enemies) {
            enemy.draw();
        }

        // 弾を全て描画する
        for (BulletBase bullet : bullets) {
            bullet.draw();
        }

        // エフェクトを全て描画する
        for (EffectBase effect : effects) {
            effect.draw();
        }

        // 画面のプレイエリア外を塗りつぶす
        {
            // 左側を塗りつぶす
            getSpriteManager().fillRect(
            // 起点のXY座標
                    0, 0,
                    // 幅・高さ
                    Define.PLAY_AREA_LEFT, Define.VIRTUAL_DISPLAY_HEIGHT,
                    // 描画色
                    Color.toColorRGBA(255, 255, 255, 255));

            // 右側を塗りつぶす
            getSpriteManager().fillRect(
            // 起点のXY座標
                    Define.PLAY_AREA_RIGHT, 0,
                    // 幅・高さ
                    Define.VIRTUAL_DISPLAY_WIDTH - Define.PLAY_AREA_RIGHT, Define.VIRTUAL_DISPLAY_HEIGHT,
                    // 描画色
                    Color.toColorRGBA(255, 255, 255, 255));
        }

        hpBar.draw(); // HPバーを描画する
        bomsInfo.draw(); // ボム情報を描画する

        joyStick.draw(); // ジョイスティックを描画する
        shotButton.draw(); // 攻撃ボタンを描画する
        bombButton.draw(); // ボムボタンを描画する
    }

    /**
     * 強制的なゲームクリアフラグを立てる
     * @param gameClear
     */
    public void setGameClear(boolean gameClear) {
        this.gameClear = gameClear;
    }

    /**
     * 全ての敵が出現済みで、敵が居なくなったらゲームクリア
     */
    @Override
    public boolean isGameclear() {
        if (gameClear) {
            return true;
        }
        return enemies.isEmpty() && stageEnemyDataList.isEmpty();
    }

    /**
     * ゲームオーバー条件は自機の撃墜のみ
     */
    @Override
    public boolean isGameover() {
        return player.isDead(); // プレイヤーが撃墜されたらゲームオーバー
    }
}