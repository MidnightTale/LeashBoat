package net.hynse.leashboat;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class LeashBoat extends JavaPlugin implements Listener {
    public NamespacedKey boatUUID = new NamespacedKey(this, "boat_uuid");
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (entity instanceof Boat boat && player.getInventory().getItemInMainHand().getType() == Material.LEAD) {
            event.setCancelled(true);
            Entity ridingEntity = boat.getVehicle();
            if (ridingEntity instanceof Axolotl chicken && !chicken.isLeashed()) {
                chicken.setLeashHolder(player);
            } else if (ridingEntity instanceof Chicken chicken && chicken.isLeashed()) {
                chicken.setLeashHolder(null);
                chicken.remove();
                if (!player.getGameMode().equals(GameMode.CREATIVE)){
                    player.getWorld().dropItem(chicken.getLocation().add(0,1,0), new ItemStack(Material.LEAD));
                }
            } else {
                ItemStack leash = player.getInventory().getItemInMainHand();

                Chicken chicken = player.getWorld().spawn(boat.getLocation(), Chicken.class);
                chicken.setLeashHolder(player);
                chicken.setSilent(true);
                chicken.setBaby();
                chicken.setAgeLock(true);
                chicken.setCollidable(false);
                chicken.setInvisible(true);
                chicken.setBreed(false);
                chicken.setLootTable(null);
                chicken.setInvulnerable(true);
                chicken.setIsChickenJockey(false);
                chicken.setEggLayTime(Integer.MAX_VALUE);
                Objects.requireNonNull(chicken.getAttribute(Attribute.GENERIC_SCALE)).setBaseValue(0.01);
                Objects.requireNonNull(chicken.getAttribute(Attribute.GENERIC_ARMOR)).setBaseValue(1024);
                Objects.requireNonNull(chicken.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(0);
                Objects.requireNonNull(chicken.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).setBaseValue(0);
                Objects.requireNonNull(chicken.getAttribute(Attribute.GENERIC_STEP_HEIGHT)).setBaseValue(1.2);
                Objects.requireNonNull(chicken.getAttribute(Attribute.GENERIC_FOLLOW_RANGE)).setBaseValue(0);
                Objects.requireNonNull(chicken.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS)).setBaseValue(1024);
                Objects.requireNonNull(chicken.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(1024);
                chicken.setHealth(1024);

                chicken.addPassenger(boat);

                chicken.setLeashHolder(player);

                if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                    leash.setAmount(leash.getAmount() - 1);
                }
                chicken.getPersistentDataContainer().set(boatUUID, PersistentDataType.STRING, boat.getUniqueId().toString());
            }

        } else if (entity instanceof Boat boat) {
            Entity ridingEntity = boat.getVehicle();
            if (ridingEntity instanceof Chicken chicken && chicken.isLeashed()) {
                if (chicken.getLeashHolder() == player) {
                    event.setCancelled(true);
                }
                chicken.remove();
                if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                    player.getWorld().dropItem(chicken.getLocation().add(0,1,0), new ItemStack(Material.LEAD));
                }
            }
        }
    }
    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Entity vehicle = event.getVehicle();
        Entity ridingEntity = vehicle.getVehicle();
        Player player = (Player) event.getAttacker();

        if (ridingEntity instanceof Chicken chicken) {
            chicken.remove();
            if (player != null && !player.getGameMode().equals(GameMode.CREATIVE) && chicken.isLeashed()) {
                player.getWorld().dropItem(chicken.getLocation().add(0,1,0), new ItemStack(Material.LEAD));
            }
        }
    }

    @EventHandler
    public void onUnleash(EntityUnleashEvent event) {
        Entity vehicle = event.getEntity().getVehicle();
        if (event.getEntity() instanceof Boat) {
        Entity ridingEntity = vehicle != null ? vehicle.getVehicle() : null;
            if (ridingEntity instanceof Chicken chicken) {
                        chicken.remove();
            }
        }
    }
}
