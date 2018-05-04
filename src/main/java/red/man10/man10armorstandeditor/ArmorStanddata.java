package red.man10.man10armorstandeditor;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class ArmorStanddata {
    Man10ArmorStandEditor plugin;
    MySQLManager mysql;
    public ArmorStanddata(Man10ArmorStandEditor plugin) {
        this.plugin = plugin;
        this.mysql = plugin.mysql;
    }
    public boolean pointadd(Player p, int add) {
        String sql = "UPDATE "+mysql.DB+".armor_point set point = point+"+add+" where uuid = '"+p.getUniqueId().toString()+"';";
        boolean done = mysql.execute(sql);
        return done;
    }
    public boolean setmaxpoint(Player p, int set) {
        String sql = "UPDATE "+mysql.DB+".armor_point set maxpoint = "+set+" where uuid = '"+p.getUniqueId().toString()+"';";
        boolean done = mysql.execute(sql);
        return done;
    }
    public boolean playercreate(Player p) {
        String sql = "INSERT INTO "+mysql.DB+".armor_point (name,uuid,point,maxpoint) VALUES ('"+p.getName()+"' ,'"+p.getUniqueId().toString()+"' , 0 , 2 );";
        boolean done = mysql.execute(sql);
        return done;
    }
    public boolean playercontain(Player p) {
        String sql = "SELECT * FROM "+mysql.DB+".armor_point WHERE uuid = '"+p.getUniqueId().toString()+"';";
        ResultSet rs = mysql.query(sql);
        if(rs==null){
            return false;
        }
        try {
            if(rs.next()) {
                // UUIDが一致するユーザが見つかった
                return true;
            }
            return false;
        } catch (SQLException e1) {
            e1.printStackTrace();
            return false;
        }
    }
    public int getplayerpoint(Player p) {
        String sql = "SELECT * FROM "+mysql.DB+".armor_point WHERE uuid = '"+p.getUniqueId().toString()+"';";
        ResultSet rs = mysql.query(sql);
        if(rs==null){
            return -1;
        }
        try {
            if(rs.next()) {
                // UUIDが一致するユーザが見つかった
                return rs.getInt("point");
            }
            return -1;
        } catch (SQLException e1) {
            e1.printStackTrace();
            return -1;
        }
    }
    public int getplayermaxpoint(Player p) {
        String sql = "SELECT * FROM "+mysql.DB+".armor_point WHERE uuid = '"+p.getUniqueId().toString()+"';";
        ResultSet rs = mysql.query(sql);
        if(rs==null){
            return -1;
        }
        try {
            if(rs.next()) {
                // UUIDが一致するユーザが見つかった
                return rs.getInt("maxpoint");
            }
            return -1;
        } catch (SQLException e1) {
            e1.printStackTrace();
            return -1;
        }
    }
    public boolean standcreate(Player p, ArmorStand stand) {
        if(!playercontain(p)){
            playercreate(p);
        }
        if(standcontain(stand)){
            p.sendMessage(plugin.prefix+"§4このスタンドはすでに登録されています！");
            return false;
        }
        if(getplayerpoint(p)==getplayermaxpoint(p)){
            p.sendMessage(plugin.prefix+"§4あなたはこれ以上スタンドを登録できません！");
            return false;
        }
        pointadd(p,1);
        stand.addScoreboardTag(p.getUniqueId().toString());
        p.sendMessage(plugin.prefix+"§a登録に成功しました。");
        return true;
    }
    public boolean standdelete(Player p, ArmorStand stand) {
        if(!standcontain(stand)&&p.hasPermission("man10ask.ignore")){
            p.getInventory().addItem(stand.getHelmet());
            p.getInventory().addItem(stand.getChestplate());
            p.getInventory().addItem(stand.getLeggings());
            p.getInventory().addItem(stand.getBoots());
            p.getInventory().addItem(stand.getEquipment().getItemInMainHand());
            p.getInventory().addItem(stand.getEquipment().getItemInOffHand());
            p.sendMessage(plugin.prefix+"§aスタンドの削除に成功しました。");
            return true;
        }else if(!standcontain(p,stand)){
            p.sendMessage(plugin.prefix+"§4このスタンドは編集できません！");
            return false;
        }
        pointadd(p,-1);
        p.getInventory().addItem(stand.getHelmet());
        p.getInventory().addItem(stand.getChestplate());
        p.getInventory().addItem(stand.getLeggings());
        p.getInventory().addItem(stand.getBoots());
        p.getInventory().addItem(stand.getEquipment().getItemInMainHand());
        p.getInventory().addItem(stand.getEquipment().getItemInOffHand());
        p.sendMessage(plugin.prefix+"§aスタンドの削除に成功しました。");
        return true;
    }
    public boolean standcontain(ArmorStand stand) {
        Set<String> list =  stand.getScoreboardTags();
        if(list.isEmpty()){
            return false;
        }else{
            return true;
        }
    }
    public boolean standcontain(Player p,ArmorStand stand) {
        Set<String> list =  stand.getScoreboardTags();
        if(list.isEmpty()){
            return false;
        }else{
            if(list.contains(p.getUniqueId().toString())) {
                return true;
            }else{
                return false;
            }
        }
    }
    public boolean standsetitem(Player p, ArmorStand stand, ItemStack item,String basyo) {
         if(!standcontain(p,stand)&&!p.hasPermission("man10ask.ignore")){
             p.sendMessage(plugin.prefix+"§4このスタンドは編集できません！");
             return false;
         }
         if(basyo.equalsIgnoreCase("helmet")) {
             stand.setHelmet(item);
         }else if(basyo.equalsIgnoreCase("plate")) {
             stand.setChestplate(item);
         }else if(basyo.equalsIgnoreCase("leggings")){
             stand.setLeggings(item);
         }else if(basyo.equalsIgnoreCase("boots")) {
             stand.setBoots(item);
         }else if(basyo.equalsIgnoreCase("mainhand")) {
             stand.getEquipment().setItemInMainHand(item);
         }else if(basyo.equalsIgnoreCase("offhand")) {
             stand.getEquipment().setItemInOffHand(item);
         }else{
             stand.setHelmet(item);
         }
         return true;
    }
    public ItemStack standgetitem(Player p, ArmorStand stand, String basyo) {
        if(!standcontain(p,stand)&&!p.hasPermission("man10ask.ignore")){
            p.sendMessage(plugin.prefix+"§4このスタンドは編集できません！");
            return null;
        }
        if(basyo.equalsIgnoreCase("helmet")) {
            return stand.getHelmet();
        }else if(basyo.equalsIgnoreCase("plate")) {
            return stand.getChestplate();
        }else if(basyo.equalsIgnoreCase("leggings")){
            return stand.getLeggings();
        }else if(basyo.equalsIgnoreCase("boots")) {
            return stand.getBoots();
        }else if(basyo.equalsIgnoreCase("mainhand")) {
            return stand.getEquipment().getItemInMainHand();
        }else if(basyo.equalsIgnoreCase("offhand")) {
            return stand.getEquipment().getItemInOffHand();
        }else{
            return stand.getHelmet();
        }
    }
    public boolean standsetvisible(Player p,ArmorStand stand) {
        if(!standcontain(p,stand)&&!p.hasPermission("man10ask.ignore")){
            p.sendMessage(plugin.prefix+"§4このスタンドは編集できません！");
            return false;
        }
        if(!stand.isVisible()) {
            stand.setVisible(true);
            p.sendMessage(plugin.prefix + "§a透明化解除に成功しました。");
            return true;
        }else{
            stand.setVisible(false);
            p.sendMessage(plugin.prefix + "§a透明化に成功しました。");
            return true;
        }
    }
    public boolean standsetgravity(Player p,ArmorStand stand) {
        if (!standcontain(p, stand)&&!p.hasPermission("man10ask.ignore")) {
            p.sendMessage(plugin.prefix + "§4このスタンドは編集できません！");
            return false;
        }
        if (!stand.hasGravity()) {
            stand.setGravity(true);
            p.sendMessage(plugin.prefix + "§a重力を有効化しました。");
            return true;
        } else {
            stand.setGravity(false);
            p.sendMessage(plugin.prefix + "§a重力を無効化しました。");
            return true;
        }
    }
    public boolean standsetbaseplate(Player p,ArmorStand stand) {
        if (!standcontain(p, stand)&&!p.hasPermission("man10ask.ignore")) {
            p.sendMessage(plugin.prefix + "§4このスタンドは編集できません！");
            return false;
        }
        if (!stand.hasBasePlate()) {
            stand.setBasePlate(true);
            p.sendMessage(plugin.prefix + "§aベースプレートを有効化しました。");
            return true;
        } else {
            stand.setBasePlate(false);
            p.sendMessage(plugin.prefix + "§aベースプレートを無効化しました。");
            return true;
        }
    }
    public boolean standsetmini(Player p,ArmorStand stand) {
        if (!standcontain(p, stand)&&!p.hasPermission("man10ask.ignore")) {
            p.sendMessage(plugin.prefix + "§4このスタンドは編集できません！");
            return false;
        }
        if (!stand.isSmall()) {
            stand.setSmall(true);
            p.sendMessage(plugin.prefix + "§a小人を有効化しました。");
            return true;
        } else {
            stand.setSmall(false);
            p.sendMessage(plugin.prefix + "§a小人を無効化しました。");
            return true;
        }
    }
    public boolean standsetdirection(Player p,ArmorStand stand) {
        if (!standcontain(p, stand)&&!p.hasPermission("man10ask.ignore")) {
            p.sendMessage(plugin.prefix + "§4このスタンドは編集できません！");
            return false;
        }
        Location loc = stand.getLocation();
        loc.setYaw(p.getLocation().getYaw());
        stand.teleport(loc);
        p.sendMessage(plugin.prefix + "§a方向を転換させました。");
        return true;
    }
    public boolean standsetlocation(Player p,ArmorStand stand) {
        if (!standcontain(p, stand)&&!p.hasPermission("man10ask.ignore")) {
            p.sendMessage(plugin.prefix + "§4このスタンドは編集できません！");
            return false;
        }
        stand.teleport(p.getLocation());
        p.sendMessage(plugin.prefix + "§aロケーションを変更しました。");
        return true;
    }
    public boolean standmove(Player p,ArmorStand stand,int x,int y,int z) {
        if (!standcontain(p, stand)&&!p.hasPermission("man10ask.ignore")) {
            p.sendMessage(plugin.prefix + "§4このスタンドは編集できません！");
            return false;
        }
        stand.teleport(stand.getLocation().add(x,y,z));
        p.sendMessage(plugin.prefix + "§a座標をx: "+x+" y: "+y+" z: "+z+"ぶん変更しました。");
        return true;
    }
}
