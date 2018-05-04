package red.man10.man10armorstandeditor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Man10ArmorStandEditor extends JavaPlugin implements Listener {
    FileConfiguration config1;
    MySQLManager mysql;
    ArmorStanddata data;
    String prefix = "§3[§dM§fa§an§f10§7Armor§6Stand§8Editor§3]§r";
    HashMap<UUID,ArmorStand> playerState;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    getServer().getPluginManager().disablePlugin(this);
                    getServer().getPluginManager().enablePlugin(this);
                    getLogger().info("設定を再読み込みしました。");
                    return true;
                }
            }
            getLogger().info("mas reload");
            return true;
        }
        Player p = (Player) sender;
        if(args.length == 0){
            p.sendMessage("==========§d§l●§f§l●§a§l●" + prefix + "§a§l●§f§l●§d§l●§r==========");
            p.sendMessage("§e--プレイヤー向けヘルプ--");
            p.sendMessage("§e石炭を持ちスタンドを右クリでGUIが開きます(登録上限に達してない場合)");
            p.sendMessage("§e新しくアイテムを設定したい場合オフハンドに設定したいアイテムをもって");
            p.sendMessage("§e右手に石炭を持ち、GUIのアイテム更新を選んでください。");
            p.sendMessage("");
            p.sendMessage("§e/mas point : [現在登録数]/[最大登録数]を表示します");
            if (p.hasPermission("man10ase.adminhelp")) {
                p.sendMessage("§c--admin向けヘルプ--");
                p.sendMessage("§c/mas setmaxp player名 設定最大point : 最大ポイントを設定します");
                p.sendMessage("§c/mas point [Player名] : [Player名]の[現在登録数]/[最大登録数]を表示します");
            }
            p.sendMessage("==========§d§l●§f§l●§a§l●" + prefix + "§a§l●§f§l●§d§l●§r==========");
            return true;
        }else if(args.length == 1){
            if(args[0].equalsIgnoreCase("point")){
                p.sendMessage(prefix+"§6"+p.getName()+"'s Point: §e"+data.getplayerpoint(p)+"§6/§e"+data.getplayermaxpoint(p));
                return true;
            }
        }else if(args.length == 2){
            if(args[0].equalsIgnoreCase("point")){
                if(!p.hasPermission("man10ase.viewotherpoint")){
                    p.sendMessage("§4あなたには権限がありません");
                    return true;
                }
                if(Bukkit.getPlayer(args[1])!=null) {
                    p.sendMessage(prefix + "§6" + Bukkit.getPlayer(args[1]).getName() + "'s Point: §e" + data.getplayerpoint(Bukkit.getPlayer(args[1])) + "§6/§e" + data.getplayermaxpoint(Bukkit.getPlayer(args[1])));
                    return true;
                }else{
                    p.sendMessage(prefix+"§cそのプレイヤーはオンラインではありません");
                    return true;
                }
            }
        }else if(args.length == 3){
            if(args[0].equalsIgnoreCase("setmaxp")){
                if(!p.hasPermission("man10ase.setmaxpoint")){
                    p.sendMessage("§4あなたには権限がありません");
                    return true;
                }
                if(Bukkit.getPlayer(args[1])==null){
                    p.sendMessage("§4そのプレイヤーはオンラインではありません");
                    return true;
                }
                int i = -1;
                try{
                    i = Integer.parseInt(args[2]);
                }catch (NumberFormatException e){
                    p.sendMessage("§4数字を入力してください");
                    return true;
                }
                if(i <= 0){
                    p.sendMessage("§41以上の数字を入力してください");
                    return true;
                }
                data.setmaxpoint(Bukkit.getPlayer(args[1]),i);
                p.sendMessage("§a成功しました。");
                return true;
            }
        }
        p.sendMessage("==========§d§l●§f§l●§a§l●" + prefix + "§a§l●§f§l●§d§l●§r==========");
        p.sendMessage("§e--プレイヤー向けヘルプ--");
        p.sendMessage("§e石炭を持ちスタンドを右クリでGUIが開きます(登録上限に達してない場合)");
        p.sendMessage("§e新しくアイテムを設定したい場合オフハンドに設定したいアイテムをもって");
        p.sendMessage("§e右手に石炭を持ち、GUIのアイテム更新を選んでください。");
        p.sendMessage("");
        p.sendMessage("§e/mas point : [現在登録数]/[最大登録数]を表示します");
        if (p.hasPermission("man10ase.adminhelp")) {
            p.sendMessage("§c--admin向けヘルプ--");
            p.sendMessage("§c/mas setmaxp player名 設定最大point : 最大ポイントを設定します");
        }
        p.sendMessage("==========§d§l●§f§l●§a§l●" + prefix + "§a§l●§f§l●§d§l●§r==========");
        return true;
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents (this,this);
        saveDefaultConfig();
        config1 = getConfig();
        mysql = new MySQLManager(this, "Mdeed");
        data = new ArmorStanddata(this);
        playerState = new HashMap<>();
        getCommand("mas").setExecutor(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    @EventHandler
    public void onRightClick(PlayerInteractAtEntityEvent event) {
        if(event.getRightClicked() instanceof ArmorStand){
            ArmorStand stand = (ArmorStand) event.getRightClicked();
            if(data.standcontain(stand)) {
                event.setCancelled(true);
            }
            if(event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.COAL)){
                if(data.standcontain(event.getPlayer(),stand)) {
                    playerState.put(event.getPlayer().getUniqueId(), stand);
                    Inventory inv = Bukkit.createInventory(null, 27, "設定メニュー");
                    ItemStack items = new ItemStack(Material.BARRIER);
                    ItemMeta itemmeta = items.getItemMeta();
                    itemmeta.setDisplayName("§c§l削除§7(クリック)§r");
                    List<String> k = new ArrayList<String>();
                    k.add("§cここをクリックでこのスタンドの");
                    k.add("§cデータを削除します。");
                    itemmeta.setLore(k);
                    items.setItemMeta(itemmeta);
                    ItemStack itemss = new ItemStack(Material.CHEST);
                    ItemMeta itemmetas = itemss.getItemMeta();
                    itemmetas.setDisplayName("§e§lアイテム設定§7(クリック)§r");
                    List<String> kk = new ArrayList<String>();
                    kk.add("§eアイテム設定は");
                    kk.add("§eこちらから。");
                    itemmetas.setLore(kk);
                    itemss.setItemMeta(itemmetas);
                    ItemStack itemsss = new ItemStack(Material.POTION);
                    ItemMeta itemmetass = itemsss.getItemMeta();
                    itemmetass.setDisplayName("§7§l透明化 on/off§7(クリック)§r");
                    List<String> kkk = new ArrayList<String>();
                    kkk.add("§e透明on/offはここをクリック！");
                    itemmetass.setLore(kkk);
                    itemsss.setItemMeta(itemmetass);
                    ItemStack itemssss = new ItemStack(Material.SAND);
                    ItemMeta itemmetasss = itemssss.getItemMeta();
                    itemmetasss.setDisplayName("§e§l重力 on/off§7(クリック)§r");
                    List<String> kkkk = new ArrayList<String>();
                    kkkk.add("§e重力on/offはここをクリック！");
                    itemmetasss.setLore(kkkk);
                    itemssss.setItemMeta(itemmetasss);
                    ItemStack itemsssss = new ItemStack(Material.OBSERVER);
                    ItemMeta itemmetassss = itemsssss.getItemMeta();
                    itemmetassss.setDisplayName("§8§l方向転換§7(クリック)§r");
                    List<String> kkkkk = new ArrayList<String>();
                    kkkkk.add("§e自分の向いている方角に向かせます!");
                    itemmetassss.setLore(kkkkk);
                    itemsssss.setItemMeta(itemmetassss);
                    ItemStack itemssssss = new ItemStack(Material.COMPASS);
                    ItemMeta itemmetasssss = itemsssss.getItemMeta();
                    itemmetasssss.setDisplayName("§c§lテレポート§7(クリック)§r");
                    List<String> kkkkkk = new ArrayList<String>();
                    kkkkkk.add("§e自分のいる場所にtpさせます！");
                    itemmetasssss.setLore(kkkkkk);
                    itemssssss.setItemMeta(itemmetasssss);
                    ItemStack itemsssssss = new ItemStack(Material.DIAMOND_BOOTS);
                    ItemMeta itemmetassssss = itemsssssss.getItemMeta();
                    itemmetassssss.setDisplayName("§c§lひだりに一歩§7(クリック)§r");
                    List<String> kkkkkkk = new ArrayList<String>();
                    kkkkkkk.add("§eひだりに一歩tpさせます！");
                    itemmetassssss.setLore(kkkkkkk);
                    itemsssssss.setItemMeta(itemmetassssss);
                    ItemStack itemssssssss = new ItemStack(Material.DIAMOND_BOOTS);
                    ItemMeta itemmetasssssss = itemssssssss.getItemMeta();
                    itemmetasssssss.setDisplayName("§c§lみぎに一歩§7(クリック)§r");
                    List<String> kkkkkkkk = new ArrayList<String>();
                    kkkkkkkk.add("§eみぎに一歩tpさせます！");
                    itemmetasssssss.setLore(kkkkkkkk);
                    itemssssssss.setItemMeta(itemmetasssssss);
                    ItemStack itemsssssssss = new ItemStack(Material.DIAMOND_BOOTS);
                    ItemMeta itemmetassssssss = itemssssssss.getItemMeta();
                    itemmetassssssss.setDisplayName("§c§lまえに一歩§7(クリック)§r");
                    List<String> kkkkkkkkk = new ArrayList<String>();
                    kkkkkkkkk.add("§eまえに一歩tpさせます！");
                    itemmetassssssss.setLore(kkkkkkkkk);
                    itemsssssssss.setItemMeta(itemmetassssssss);
                    ItemStack itemssssssssss = new ItemStack(Material.DIAMOND_BOOTS);
                    ItemMeta itemmetasssssssss = itemsssssssss.getItemMeta();
                    itemmetasssssssss.setDisplayName("§c§lうしろに一歩§7(クリック)§r");
                    List<String> kkkkkkkkkk = new ArrayList<String>();
                    kkkkkkkkkk.add("§eうしろに一歩tpさせます！");
                    itemmetasssssssss.setLore(kkkkkkkkkk);
                    itemssssssssss.setItemMeta(itemmetasssssssss);
                    ItemStack itemsssssssssss = new ItemStack(Material.STEP);
                    ItemMeta itemmetassssssssss = itemssssssssss.getItemMeta();
                    itemmetassssssssss.setDisplayName("§7§lベースプレートon/off§7(クリック)§r");
                    List<String> kkkkkkkkkkk = new ArrayList<String>();
                    kkkkkkkkkkk.add("§eベースプレートのon/offを切り替えます！");
                    itemmetassssssssss.setLore(kkkkkkkkkkk);
                    itemsssssssssss.setItemMeta(itemmetassssssssss);
                    ItemStack itemssssssssssss = new ItemStack(Material.DIAMOND_HOE,1,(short)890);
                    ItemMeta itemmetasssssssssss = itemsssssssssss.getItemMeta();
                    itemmetasssssssssss.setUnbreakable(true);
                    itemmetasssssssssss.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    itemmetassssssssss.setDisplayName("§6§l小人on/off§7(クリック)§r");
                    List<String> kkkkkkkkkkkk = new ArrayList<String>();
                    kkkkkkkkkkkk.add("§e小人化のon/offを切り替えます！");
                    itemmetasssssssssss.setLore(kkkkkkkkkkkk);
                    itemssssssssssss.setItemMeta(itemmetasssssssssss);
                    inv.setItem(1, items);
                    inv.setItem(3, itemss);
                    inv.setItem(5, itemsss);
                    inv.setItem(7, itemssss);
                    inv.setItem(10, itemsssss);
                    inv.setItem(12, itemssssss);
                    inv.setItem(14, itemsssssssssss);
                    inv.setItem(16, itemssssssssssss);
                    inv.setItem(19, itemsssssss);
                    inv.setItem(21, itemssssssss);
                    inv.setItem(23, itemsssssssss);
                    inv.setItem(25, itemssssssssss);
                    event.getPlayer().openInventory(inv);
                }else if(event.getPlayer().hasPermission("man10ask.ignore")){
                    event.getPlayer().sendMessage(prefix+"§c§lOP権限で開きました");
                    playerState.put(event.getPlayer().getUniqueId(),stand);
                    Inventory inv = Bukkit.createInventory(null, 27, "設定メニュー");
                    ItemStack items = new ItemStack(Material.BARRIER);
                    ItemMeta itemmeta = items.getItemMeta();
                    itemmeta.setDisplayName("§c§l削除§7(クリック)§r");
                    List<String> k = new ArrayList<String>();
                    k.add("§cここをクリックでこのスタンドの");
                    k.add("§cデータを削除します。");
                    itemmeta.setLore(k);
                    items.setItemMeta(itemmeta);
                    ItemStack itemss = new ItemStack(Material.CHEST);
                    ItemMeta itemmetas = itemss.getItemMeta();
                    itemmetas.setDisplayName("§e§lアイテム設定§7(クリック)§r");
                    List<String> kk = new ArrayList<String>();
                    kk.add("§eアイテム設定は");
                    kk.add("§eこちらから。");
                    itemmetas.setLore(kk);
                    itemss.setItemMeta(itemmetas);
                    ItemStack itemsss = new ItemStack(Material.POTION);
                    ItemMeta itemmetass = itemsss.getItemMeta();
                    itemmetass.setDisplayName("§7§l透明化 on/off§7(クリック)§r");
                    List<String> kkk = new ArrayList<String>();
                    kkk.add("§e透明on/offはここをクリック！");
                    itemmetass.setLore(kkk);
                    itemsss.setItemMeta(itemmetass);
                    ItemStack itemssss = new ItemStack(Material.SAND);
                    ItemMeta itemmetasss = itemssss.getItemMeta();
                    itemmetasss.setDisplayName("§e§l重力 on/off§7(クリック)§r");
                    List<String> kkkk = new ArrayList<String>();
                    kkkk.add("§e重力on/offはここをクリック！");
                    itemmetasss.setLore(kkkk);
                    itemssss.setItemMeta(itemmetasss);
                    ItemStack itemsssss = new ItemStack(Material.OBSERVER);
                    ItemMeta itemmetassss = itemsssss.getItemMeta();
                    itemmetassss.setDisplayName("§8§l方向転換§7(クリック)§r");
                    List<String> kkkkk = new ArrayList<String>();
                    kkkkk.add("§e自分の向いている方角に向かせます!");
                    itemmetassss.setLore(kkkkk);
                    itemsssss.setItemMeta(itemmetassss);
                    ItemStack itemssssss = new ItemStack(Material.COMPASS);
                    ItemMeta itemmetasssss = itemsssss.getItemMeta();
                    itemmetasssss.setDisplayName("§c§lテレポート§7(クリック)§r");
                    List<String> kkkkkk = new ArrayList<String>();
                    kkkkkk.add("§e自分のいる場所にtpさせます！");
                    itemmetasssss.setLore(kkkkkk);
                    itemssssss.setItemMeta(itemmetasssss);
                    ItemStack itemsssssss = new ItemStack(Material.DIAMOND_BOOTS);
                    ItemMeta itemmetassssss = itemsssssss.getItemMeta();
                    itemmetassssss.setDisplayName("§c§lひだりに一歩§7(クリック)§r");
                    List<String> kkkkkkk = new ArrayList<String>();
                    kkkkkkk.add("§eひだりに一歩tpさせます！");
                    itemmetassssss.setLore(kkkkkkk);
                    itemsssssss.setItemMeta(itemmetassssss);
                    ItemStack itemssssssss = new ItemStack(Material.DIAMOND_BOOTS);
                    ItemMeta itemmetasssssss = itemssssssss.getItemMeta();
                    itemmetasssssss.setDisplayName("§c§lみぎに一歩§7(クリック)§r");
                    List<String> kkkkkkkk = new ArrayList<String>();
                    kkkkkkkk.add("§eみぎに一歩tpさせます！");
                    itemmetasssssss.setLore(kkkkkkkk);
                    itemssssssss.setItemMeta(itemmetasssssss);
                    ItemStack itemsssssssss = new ItemStack(Material.DIAMOND_BOOTS);
                    ItemMeta itemmetassssssss = itemssssssss.getItemMeta();
                    itemmetassssssss.setDisplayName("§c§lまえに一歩§7(クリック)§r");
                    List<String> kkkkkkkkk = new ArrayList<String>();
                    kkkkkkkkk.add("§eまえに一歩tpさせます！");
                    itemmetassssssss.setLore(kkkkkkkkk);
                    itemsssssssss.setItemMeta(itemmetassssssss);
                    ItemStack itemssssssssss = new ItemStack(Material.DIAMOND_BOOTS);
                    ItemMeta itemmetasssssssss = itemsssssssss.getItemMeta();
                    itemmetasssssssss.setDisplayName("§c§lうしろに一歩§7(クリック)§r");
                    List<String> kkkkkkkkkk = new ArrayList<String>();
                    kkkkkkkkkk.add("§eうしろに一歩tpさせます！");
                    itemmetasssssssss.setLore(kkkkkkkkkk);
                    itemssssssssss.setItemMeta(itemmetasssssssss);
                    ItemStack itemsssssssssss = new ItemStack(Material.STEP);
                    ItemMeta itemmetassssssssss = itemssssssssss.getItemMeta();
                    itemmetassssssssss.setDisplayName("§7§lベースプレートon/off§7(クリック)§r");
                    List<String> kkkkkkkkkkk = new ArrayList<String>();
                    kkkkkkkkkkk.add("§eベースプレートのon/offを切り替えます！");
                    itemmetassssssssss.setLore(kkkkkkkkkkk);
                    itemsssssssssss.setItemMeta(itemmetassssssssss);
                    ItemStack itemssssssssssss = new ItemStack(Material.DIAMOND_HOE,1,(short)890);
                    ItemMeta itemmetasssssssssss = itemsssssssssss.getItemMeta();
                    itemmetasssssssssss.setUnbreakable(true);
                    itemmetasssssssssss.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    itemmetassssssssss.setDisplayName("§6§l小人on/off§7(クリック)§r");
                    List<String> kkkkkkkkkkkk = new ArrayList<String>();
                    kkkkkkkkkkkk.add("§e小人化のon/offを切り替えます！");
                    itemmetasssssssssss.setLore(kkkkkkkkkkkk);
                    itemssssssssssss.setItemMeta(itemmetasssssssssss);
                    inv.setItem(1,items);
                    inv.setItem(3,itemss);
                    inv.setItem(5,itemsss);
                    inv.setItem(7,itemssss);
                    inv.setItem(10,itemsssss);
                    inv.setItem(12,itemssssss);
                    inv.setItem(14,itemsssssssssss);
                    inv.setItem(16,itemssssssssssss);
                    inv.setItem(19,itemsssssss);
                    inv.setItem(21,itemssssssss);
                    inv.setItem(23,itemsssssssss);
                    inv.setItem(25,itemssssssssss);
                    event.getPlayer().openInventory(inv);
                }else{
                    if(data.standcreate(event.getPlayer(), stand)){
                        playerState.put(event.getPlayer().getUniqueId(),stand);
                        Inventory inv = Bukkit.createInventory(null, 27, "設定メニュー");
                        ItemStack items = new ItemStack(Material.BARRIER);
                        ItemMeta itemmeta = items.getItemMeta();
                        itemmeta.setDisplayName("§c§l削除§7(クリック)§r");
                        List<String> k = new ArrayList<String>();
                        k.add("§cここをクリックでこのスタンドの");
                        k.add("§cデータを削除します。");
                        itemmeta.setLore(k);
                        items.setItemMeta(itemmeta);
                        ItemStack itemss = new ItemStack(Material.CHEST);
                        ItemMeta itemmetas = itemss.getItemMeta();
                        itemmetas.setDisplayName("§e§lアイテム設定§7(クリック)§r");
                        List<String> kk = new ArrayList<String>();
                        kk.add("§eアイテム設定は");
                        kk.add("§eこちらから。");
                        itemmetas.setLore(kk);
                        itemss.setItemMeta(itemmetas);
                        ItemStack itemsss = new ItemStack(Material.POTION);
                        ItemMeta itemmetass = itemsss.getItemMeta();
                        itemmetass.setDisplayName("§7§l透明化 on/off§7(クリック)§r");
                        List<String> kkk = new ArrayList<String>();
                        kkk.add("§e透明on/offはここをクリック！");
                        itemmetass.setLore(kkk);
                        itemsss.setItemMeta(itemmetass);
                        ItemStack itemssss = new ItemStack(Material.SAND);
                        ItemMeta itemmetasss = itemssss.getItemMeta();
                        itemmetasss.setDisplayName("§e§l重力 on/off§7(クリック)§r");
                        List<String> kkkk = new ArrayList<String>();
                        kkkk.add("§e重力on/offはここをクリック！");
                        itemmetasss.setLore(kkkk);
                        itemssss.setItemMeta(itemmetasss);
                        ItemStack itemsssss = new ItemStack(Material.OBSERVER);
                        ItemMeta itemmetassss = itemsssss.getItemMeta();
                        itemmetassss.setDisplayName("§8§l方向転換§7(クリック)§r");
                        List<String> kkkkk = new ArrayList<String>();
                        kkkkk.add("§e自分の向いている方角に向かせます!");
                        itemmetassss.setLore(kkkkk);
                        itemsssss.setItemMeta(itemmetassss);
                        ItemStack itemssssss = new ItemStack(Material.COMPASS);
                        ItemMeta itemmetasssss = itemsssss.getItemMeta();
                        itemmetasssss.setDisplayName("§c§lテレポート§7(クリック)§r");
                        List<String> kkkkkk = new ArrayList<String>();
                        kkkkkk.add("§e自分のいる場所にtpさせます！");
                        itemmetasssss.setLore(kkkkkk);
                        itemssssss.setItemMeta(itemmetasssss);
                        ItemStack itemsssssss = new ItemStack(Material.DIAMOND_BOOTS);
                        ItemMeta itemmetassssss = itemsssssss.getItemMeta();
                        itemmetassssss.setDisplayName("§c§lひだりに一歩§7(クリック)§r");
                        List<String> kkkkkkk = new ArrayList<String>();
                        kkkkkkk.add("§eひだりに一歩tpさせます！");
                        itemmetassssss.setLore(kkkkkkk);
                        itemsssssss.setItemMeta(itemmetassssss);
                        ItemStack itemssssssss = new ItemStack(Material.DIAMOND_BOOTS);
                        ItemMeta itemmetasssssss = itemssssssss.getItemMeta();
                        itemmetasssssss.setDisplayName("§c§lみぎに一歩§7(クリック)§r");
                        List<String> kkkkkkkk = new ArrayList<String>();
                        kkkkkkkk.add("§eみぎに一歩tpさせます！");
                        itemmetasssssss.setLore(kkkkkkkk);
                        itemssssssss.setItemMeta(itemmetasssssss);
                        ItemStack itemsssssssss = new ItemStack(Material.DIAMOND_BOOTS);
                        ItemMeta itemmetassssssss = itemssssssss.getItemMeta();
                        itemmetassssssss.setDisplayName("§c§lまえに一歩§7(クリック)§r");
                        List<String> kkkkkkkkk = new ArrayList<String>();
                        kkkkkkkkk.add("§eまえに一歩tpさせます！");
                        itemmetassssssss.setLore(kkkkkkkkk);
                        itemsssssssss.setItemMeta(itemmetassssssss);
                        ItemStack itemssssssssss = new ItemStack(Material.DIAMOND_BOOTS);
                        ItemMeta itemmetasssssssss = itemsssssssss.getItemMeta();
                        itemmetasssssssss.setDisplayName("§c§lうしろに一歩§7(クリック)§r");
                        List<String> kkkkkkkkkk = new ArrayList<String>();
                        kkkkkkkkkk.add("§eうしろに一歩tpさせます！");
                        itemmetasssssssss.setLore(kkkkkkkkkk);
                        itemssssssssss.setItemMeta(itemmetasssssssss);
                        ItemStack itemsssssssssss = new ItemStack(Material.STEP);
                        ItemMeta itemmetassssssssss = itemssssssssss.getItemMeta();
                        itemmetassssssssss.setDisplayName("§7§lベースプレートon/off§7(クリック)§r");
                        List<String> kkkkkkkkkkk = new ArrayList<String>();
                        kkkkkkkkkkk.add("§eベースプレートのon/offを切り替えます！");
                        itemmetassssssssss.setLore(kkkkkkkkkkk);
                        itemsssssssssss.setItemMeta(itemmetassssssssss);
                        ItemStack itemssssssssssss = new ItemStack(Material.DIAMOND_HOE,1,(short)890);
                        ItemMeta itemmetasssssssssss = itemsssssssssss.getItemMeta();
                        itemmetasssssssssss.setUnbreakable(true);
                        itemmetasssssssssss.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                        itemmetassssssssss.setDisplayName("§6§l小人on/off§7(クリック)§r");
                        List<String> kkkkkkkkkkkk = new ArrayList<String>();
                        kkkkkkkkkkkk.add("§e小人化のon/offを切り替えます！");
                        itemmetasssssssssss.setLore(kkkkkkkkkkkk);
                        itemssssssssssss.setItemMeta(itemmetasssssssssss);
                        inv.setItem(1,items);
                        inv.setItem(3,itemss);
                        inv.setItem(5,itemsss);
                        inv.setItem(7,itemssss);
                        inv.setItem(10,itemsssss);
                        inv.setItem(12,itemssssss);
                        inv.setItem(14,itemsssssssssss);
                        inv.setItem(16,itemssssssssssss);
                        inv.setItem(19,itemsssssss);
                        inv.setItem(21,itemssssssss);
                        inv.setItem(23,itemsssssssss);
                        inv.setItem(25,itemssssssssss);
                        event.getPlayer().openInventory(inv);
                    }
                }
            }
        }
    }
    @EventHandler
    public void onclick(InventoryClickEvent e){
        Player p= (Player) e.getWhoClicked();
        if(playerState.containsKey(p.getUniqueId())) {
            if(e.getClickedInventory().getName().equalsIgnoreCase("設定メニュー")) {
                e.setCancelled(true);
                if(e.getClickedInventory()==p.getInventory()) {
                    return;
                }
                if (e.getSlot() == 1 || e.getSlot() == 3 || e.getSlot() == 5 || e.getSlot() == 7 || e.getSlot() == 10 || e.getSlot() == 12 || e.getSlot() == 14 || e.getSlot() == 16 || e.getSlot() == 19 || e.getSlot() == 21 || e.getSlot() == 23 || e.getSlot() == 25) {
                    ArmorStand stand = playerState.get(p.getUniqueId());
                    if (e.getSlot() == 1) {
                        if(data.standdelete(p, stand)) {
                            stand.remove();
                        }
                        p.closeInventory();
                    } else if (e.getSlot() == 3) {
                        p.closeInventory();
                        playerState.put(p.getUniqueId(),stand);
                        Inventory inv = Bukkit.createInventory(null, 18, "インベントリ設定");
                        ItemStack blackpan = new ItemStack(Material.STAINED_GLASS_PANE,1,(short)15);
                        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
                        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
                        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
                        ItemStack boot = new ItemStack(Material.DIAMOND_BOOTS);
                        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
                        ItemStack shield= new ItemStack(Material.SHIELD);
                        inv.setItem(0,helmet);
                        inv.setItem(1,chestplate);
                        inv.setItem(2,leggings);
                        inv.setItem(3,boot);
                        inv.setItem(4,sword);
                        inv.setItem(5,shield);
                        inv.setItem(6,blackpan);
                        inv.setItem(7,blackpan);
                        inv.setItem(8,blackpan);
                        inv.setItem(9,data.standgetitem(p,stand,"helmet"));
                        inv.setItem(10,data.standgetitem(p,stand,"plate"));
                        inv.setItem(11,data.standgetitem(p,stand,"leggings"));
                        inv.setItem(12,data.standgetitem(p,stand,"boots"));
                        inv.setItem(13,data.standgetitem(p,stand,"mainhand"));
                        inv.setItem(14,data.standgetitem(p,stand,"offhand"));
                        inv.setItem(15,blackpan);
                        inv.setItem(16,blackpan);
                        inv.setItem(17,blackpan);
                        p.openInventory(inv);
                    } else if (e.getSlot() == 5) {
                        data.standsetvisible(p, stand);
                        p.closeInventory();
                    } else if (e.getSlot() == 7) {
                        data.standsetgravity(p, stand);
                        p.closeInventory();
                    } else if (e.getSlot() == 10) {
                        data.standsetdirection(p, stand);
                        p.closeInventory();
                    } else if (e.getSlot() == 12) {
                        data.standsetlocation(p, stand);
                        p.closeInventory();
                    } else if (e.getSlot() == 14) {
                        data.standsetbaseplate(p,stand);
                        p.closeInventory();
                    } else if (e.getSlot() == 16) {
                        data.standsetmini(p, stand);
                        p.closeInventory();
                    } else if (e.getSlot() == 19) {
                        data.standmove(p, stand, 1, 0, 0);
                        p.closeInventory();
                    } else if (e.getSlot() == 21) {
                        data.standmove(p, stand, -1, 0, 0);
                        p.closeInventory();
                    } else if (e.getSlot() == 23) {
                        data.standmove(p, stand, 0, 0, 1);
                        p.closeInventory();
                    } else if (e.getSlot() == 25) {
                        data.standmove(p, stand, 0, 0, -1);
                        p.closeInventory();
                    }
                }
            }else if(e.getClickedInventory().getName().equalsIgnoreCase("インベントリ設定")) {
                if(e.getClickedInventory()==p.getInventory()) {
                    return;
                }
                if ((e.getSlot() >= 0&&e.getSlot() <= 8)||(e.getSlot() >= 15&&e.getSlot() <= 17)){
                    e.setCancelled(true);
                }
            }
        }
    }
    @EventHandler
    public void onLogin(PlayerLoginEvent e){
        Player p =  e.getPlayer();
        if(!data.playercontain(p)){
            data.playercreate(p);
        }
    }
    @EventHandler
    public void onCloseInventory(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        if (playerState.containsKey(p.getUniqueId())) {
            if(e.getInventory().getName().equalsIgnoreCase("インベントリ設定")){
                data.standsetitem(p, playerState.get(p.getUniqueId()),e.getInventory().getItem(9),"helmet");
                data.standsetitem(p, playerState.get(p.getUniqueId()),e.getInventory().getItem(10),"plate");
                data.standsetitem(p, playerState.get(p.getUniqueId()),e.getInventory().getItem(11),"leggings");
                data.standsetitem(p, playerState.get(p.getUniqueId()),e.getInventory().getItem(12),"boots");
                data.standsetitem(p, playerState.get(p.getUniqueId()),e.getInventory().getItem(13),"mainhand");
                data.standsetitem(p, playerState.get(p.getUniqueId()),e.getInventory().getItem(14),"offhand");
                p.sendMessage(prefix+"§aアイテムセットに成功しました。");
            }
            playerState.remove(p.getUniqueId());
        }
    }
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity()instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand)event.getEntity();
            if(data.standcontain(stand)){
                event.setCancelled(true);
            }
        }
    }
}
