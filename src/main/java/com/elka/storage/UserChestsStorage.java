package com.elka.storage;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author kkurdupov
 */
public class UserChestsStorage {

    private static class UserChestsStorageHolder {

        private static UserChestsStorage INSTANCE = new UserChestsStorage();
    }
    public static JSONArray definedUsers;
    private Map<String, JSONObject> userChests = new ConcurrentHashMap<>();

    private UserChestsStorage() {
    }

    static {
        try {
            definedUsers = new JSONArray("[{\"userId\":1454976,\"sUserId\":\"115811776\","
                    + "\"level\":173,\"loginTime\":1433142505,\"likeStatus\":0,\"hash\":\"1cead19985b6328f\","
                    + "\"photo\":\"http://cs624820.vk.me/v624820776/2c069/pbRRN0-pG3Q.jpg\",\"name\":\"Каролина Савельева\"},"
                    + "{\"userId\":6591130,\"sUserId\":\"12143235\",\"level\":85,\"loginTime\":1433219274,\"likeStatus\":0,"
                    + "\"hash\":\"00fa954e96d3fb4a\",\"photo\":\"http://cs5466.vk.me/u12143235/e_66836e1e.jpg\","
                    + "\"name\":\"Константин Курдупов\"},{\"userId\":2883430,\"sUserId\":\"12482981\",\"level\":186,"
                    + "\"loginTime\":1433193329,\"likeStatus\":0,\"hash\":\"13374f84728d0675\","
                    + "\"photo\":\"http://cs10490.vk.me/u12482981/e_c13a6021.jpg\",\"name\":\"Денис Малышкин\"},"
                    + "{\"userId\":6676640,\"sUserId\":\"139179027\",\"level\":1,\"loginTime\":1427975456,\"likeStatus\":0,"
                    + "\"hash\":\"86051ebc7f2def54\",\"photo\":\"http://cs625730.vk.me/v625730027/3706a/wCNc32El9xc.jpg\","
                    + "\"name\":\"Елена Александровна\"},{\"userId\":69139,\"sUserId\":\"166552269\",\"level\":210,"
                    + "\"loginTime\":1432984444,\"likeStatus\":0,\"hash\":\"72156e2bd85ce212\","
                    + "\"photo\":\"http://cs623227.vk.me/v623227269/2bd2b/QZM1BkVYDoM.jpg\",\"name\":\"Аня Дербенёва\"},"
                    + "{\"userId\":6003663,\"sUserId\":\"249336702\",\"level\":152,\"loginTime\":1433225609,\"likeStatus\":0,"
                    + "\"hash\":\"0571b79d361b2916\",\"photo\":\"http://cs621724.vk.me/v621724702/e3b2/YlK13bIfnO4.jpg\","
                    + "\"name\":\"Татьяна Ефременко\"},{\"userId\":284549,\"sUserId\":\"3713861\",\"level\":48,"
                    + "\"loginTime\":1424583992,\"likeStatus\":0,\"hash\":\"e2ad1d38634b7f80\","
                    + "\"photo\":\"http://cs10553.vk.me/u3713861/e_f8871a63.jpg\",\"name\":\"Павел Лысенко\"},"
                    + "{\"userId\":459639,\"sUserId\":\"41034465\",\"level\":270,\"loginTime\":1433146505,\"likeStatus\":0,"
                    + "\"hash\":\"b3d7ef796d5b9669\",\"photo\":\"http://cs613417.vk.me/v613417465/10f62/lV32TfIFaNo.jpg\","
                    + "\"name\":\"Ксюшенька Ратушина\"},{\"userId\":1070767,\"sUserId\":\"66721249\",\"level\":56,"
                    + "\"loginTime\":1433209091,\"likeStatus\":0,\"hash\":\"f43df328ac1521e7\","
                    + "\"photo\":\"http://cs620828.vk.me/v620828249/1a9df/DdU3945F3Rc.jpg\",\"name\":\"Ирина Ринкевич\"},"
                    + "{\"userId\":3017348,\"sUserId\":\"70089428\",\"level\":151,\"loginTime\":1433214407,\"likeStatus\":0,"
                    + "\"hash\":\"49b9a8d168c5f1f4\",\"photo\":\"http://cs629520.vk.me/v629520428/4478/ygspJnjwrsk.jpg\","
                    + "\"name\":\"Елена Эрлих\"},{\"userId\":222502,\"sUserId\":\"7307137\",\"level\":105,"
                    + "\"loginTime\":1433136508,\"likeStatus\":0,\"hash\":\"324472df2922313a\","
                    + "\"photo\":\"http://cs622416.vk.me/v622416137/33783/BQJlXYTfaWg.jpg\",\"name\":\"Екатерина Садовская\"},"
                    + "{\"userId\":1480668,\"sUserId\":\"81945095\",\"level\":4,\"loginTime\":1431582854,\"likeStatus\":0,"
                    + "\"hash\":\"aa011eae6ad83d65\",\"photo\":\"http://cs605621.vk.me/v605621095/b309/3sTKB3vbqYk.jpg\","
                    + "\"name\":\"Алена Шилкова\"},{\"userId\":\"santa\",\"sUserId\":\"santa\",\"level\":4,\"loginTime\":1431582854,\"likeStatus\":0,"
                    + "\"hash\":\"aa011eae6ad83d65\",\"photo\":\"\","
                    + "\"name\":\"Дед мороз\"}, {"
                    + "\"name\":\"Александра Соловьёва\","
                    + "\"photo\":\"http://cs623826.vk.me/v623826384/4228c/PtwlsJVToUs.jpg\","
                    + "\"userId\": 2225356,"
                    + "\"sUserId\": \"215809384\","
                    + "\"level\": 146,"
                    + " \"loginTime\": 1434143193,"
                    + "\"likeStatus\": 0,"
                    + "\"hash\": \"a81d0dcc107022cf\""
                    + "}]");
        } catch (JSONException ex) {
            Logger.getLogger(UserChestsStorage.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void put(String userId, JSONObject json) {
        userChests.put(userId, json);
    }

    public JSONObject remove(String userId) {
        return userChests.remove(userId);
    }

    public Collection<JSONObject> values() {
        return userChests.values();
    }

    public static UserChestsStorage getInstance() {
        return UserChestsStorageHolder.INSTANCE;
    }
}
