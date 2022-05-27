package me.ahmetdev510;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.bukkit.configuration.file.YamlConfiguration.*;

public class WardenMob extends JavaPlugin implements Listener {


    public FileConfiguration config = getConfig();


    public static HashMap<String, String> messageData = new HashMap<String, String>();


    @Override
    public void onEnable() {
        config.set("wardenmob.wardenspawn", 30);
        config.options().copyDefaults(true);
        saveConfig();

        getLogger().info(getName() + " Active!");
        getCommand("wardenmob").setExecutor(this);
        this.getServer().getPluginManager().registerEvents(this,this);
    }


    @Override
    public void onDisable() {
        getLogger().info(getName() + " Deactivated!");
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player) sender;
        if (cmd.getLabel().equalsIgnoreCase("wardenmob")) {
            if (sender instanceof Player) {
                LivingEntity mob = (LivingEntity) p.getWorld().spawnEntity(p.getLocation(), EntityType.IRON_GOLEM);
                IronGolem z = (IronGolem) mob;
                z.setCustomName("§cWarden");
                z.setCustomNameVisible(config.getBoolean("wardenmob.wardenNameVisible"));
                z.setHealth(config.getInt("wardenmob.health"));
                p.playSound(p.getLocation(), "minecraft:wardenidle", SoundCategory.MASTER, 100, 1);
                sender.sendMessage("§6§lMOB: §fMob warden spawned!");
            }
        }
        return true;
    }


    @EventHandler
    public void onEntityDeath(EntityDeathEvent e)
    {
        if(e.getEntity().getType().getName().equalsIgnoreCase("iron_golem")) {
            if(e.getEntity().getCustomName() == null) {
                return;
            }else{
                if(e.getEntity().getCustomName().equalsIgnoreCase("§cWarden")) {
                    e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), new ItemStack(Material.getMaterial(config.getString("wardenmob.drop.dropitem")),config.getInt("wardenmob.drop.dropsize")));

                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent e) {
        Player p = e.getPlayer();
        if (p.getItemInHand().getType() == Material.NAME_TAG) {
            String oldName = e.getRightClicked().getCustomName();
            if(oldName == null) {
                if(e.getRightClicked().getType().getTypeId() == 99) {
                    if(e.getPlayer().getInventory().getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase("Warden")) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                e.getRightClicked().remove();
                            }
                        }.runTaskLater(this, 1);

                    }
                }
            }
        }
    }

    private List<UUID> Sneaking = new ArrayList<UUID>();
    private List<UUID> WardenAttack = new ArrayList<UUID>();

    @EventHandler
    public void move(PlayerMoveEvent event) {
        Player p = (Player) event.getPlayer();

        if(Sneaking.contains(p.getUniqueId())) {
            return;
        }else{
            event.getPlayer().getWorld().getNearbyEntities(p.getLocation(), 10, 10, 10, entity -> entity instanceof Mob).forEach(entity -> {
                if(entity.getType().getName().equalsIgnoreCase("iron_golem")) {
                    if(entity.getCustomName() == null) {
                        return;
                    }else{
                        if(entity.getCustomName().equalsIgnoreCase("§cWarden")) {
                            if(config.getBoolean("wardenmob.sound") == true) {

                                    IronGolem golem = (IronGolem) entity;
                                    if(WardenAttack.contains(p.getUniqueId())) {
                                        golem.setTarget(Bukkit.getPlayer(p.getUniqueId()));
                                    }else{
                                        if(p.getGameMode().equals(GameMode.CREATIVE) || p.getGameMode().equals(GameMode.SPECTATOR)) {
                                            return;
                                        }else{
                                            p.playSound(p.getLocation(), "minecraft:wardenidle", SoundCategory.MASTER, 100, 1);
                                            WardenAttack.add(p.getUniqueId());
                                        }

                                    }


                            }else{
                                IronGolem golem = (IronGolem) entity;
                                if(WardenAttack.contains(p.getUniqueId())) {
                                    golem.setTarget(Bukkit.getPlayer(p.getUniqueId()));
                                }else{
                                    if(p.getGameMode().equals(GameMode.CREATIVE) || p.getGameMode().equals(GameMode.SPECTATOR)) {
                                        return;
                                    }else{
                                        WardenAttack.add(p.getUniqueId());
                                    }

                                }
                            }

                        }
                    }
                }
            });
        }
    }


    @EventHandler
    public void Sneaking(PlayerToggleSneakEvent event) {
        Player p = (Player) event.getPlayer();
        if (!p.isSneaking()) {
            if(Sneaking.contains(p.getUniqueId())) {
                return;
            }
            Sneaking.add(p.getUniqueId());
            return;
        }
        if(WardenAttack.contains(p.getUniqueId())) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    WardenAttack.clear();
                }
            }.runTaskLater(this, 600L);
        }
        Sneaking.clear();
    }

    @EventHandler
    public void wardendamage(EntityDamageEvent e) {
        if(e.getEntity() instanceof IronGolem) {
            if(e.getEntity().getType().getName().equalsIgnoreCase("iron_golem")) {
                if(e.getEntity().getCustomName() == null) {
                    return;
                }else{
                    if(e.getEntity().getCustomName().equalsIgnoreCase("§cWarden")) {
                        if(e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                            return;
                        }else{
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }


    @EventHandler
    public void wardenMob(EntityTargetEvent event) {
        if(event.getTarget() == null) return;
        if(event.getTarget() instanceof Player) {
            return;
        }else{

            if(event.getEntity().getType().getName().equalsIgnoreCase("iron_golem")) {
                if(event.getEntity().getCustomName() == null) {
                    return;
                }else{
                    if(event.getEntity().getCustomName().equalsIgnoreCase("§cWarden")) {
                        event.setCancelled(true);
                    }
                }
            }

            if(event.getTarget().getType().getName().equalsIgnoreCase("iron_golem")) {
                if(event.getTarget().getCustomName() == null) {
                    return;
                }else{
                    if(event.getTarget().getCustomName().equalsIgnoreCase("§cWarden")) {
                        event.setCancelled(true);
                    }
                }
            }


        }
    }


    @EventHandler
    public void spawnWarden(EntitySpawnEvent event) {
        if(!(event.getEntity() instanceof Monster))
            return;
        if(event.getLocation().getBlock().isLiquid())
            return;

        Random ran = new Random();
        int choice = ran.nextInt(100) + 1;
        if (choice < config.getInt("wardenmob.spawnschance")) {
            World world = event.getLocation().getWorld();
            LivingEntity mob = (LivingEntity) world.spawnEntity(event.getLocation(), EntityType.IRON_GOLEM);
            IronGolem z = (IronGolem) mob;
            z.setCustomName("§cWarden");
            z.setCustomNameVisible(config.getBoolean("wardenmob.wardenNameVisible"));
            z.setHealth(config.getInt("wardenmob.health"));
        }

    }


}