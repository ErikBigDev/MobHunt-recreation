package io.github.erikbigdev.mobhunt;

import java.util.AbstractMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Jigsaw;
import org.bukkit.block.data.type.Jigsaw.Orientation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public final class MobHunt extends JavaPlugin implements Listener{
	public MobHunt() {
		instance = this;
	}

	public static MobHunt instance;
	
	@Override
	public void onEnable() {
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		//Bukkit.getServer().getPluginManager().registerEvents(new PacketInjection(), this);
		World world = getServer().getWorld("world");
		
//		world.setGameRule(GameRule.DO_MOB_SPAWNING , false);
//		world.setGameRule(GameRule.DO_PATROL_SPAWNING , false);
//		world.setGameRule(GameRule.DO_TRADER_SPAWNING , false);
		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE , false);
		world.setGameRule(GameRule.DO_WEATHER_CYCLE , false);
		
		Location loc = world.getSpawnLocation();
		corners1 = CreateBox(loc.clone().subtract(7.0, 0.0, 0.0));
		corners2 = CreateBox(loc.clone().add(7.0, 0.0, 0.0));
		
		as1 = (ArmorStand) world.spawnEntity(loc.clone().subtract(7.0, 0.0, 0.0), EntityType.ARMOR_STAND);
		as2 = (ArmorStand) world.spawnEntity(loc.clone().add(7.0, 0.0, 0.0), EntityType.ARMOR_STAND);
		
		as1.setGravity(false);
		as1.setInvisible(true);
		as1.setInvulnerable(true);
		as1.setSmall(true);
		as1.setCustomNameVisible(true);
		if(player1 != null)
			as1.setCustomName(player1.getName());
		
		as2.setGravity(false);
		as2.setInvisible(true);
		as2.setInvulnerable(true);
		as2.setSmall(true);
		as2.setCustomNameVisible(true);
		if(player2 != null)
			as2.setCustomName(player2.getName());
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if (player1 == null) {
			player1 = event.getPlayer();
			as1.setCustomName(player1.getName());
			}
		else if(player2 == null) {
			player2 = event.getPlayer();
			as2.setCustomName(player2.getName());
		}
		event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20*60*60, 255));
		event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20*60*60, 255));
		
	}

	@EventHandler
	public void onDisconnect(PlayerQuitEvent event) {
		if(player1 == event.getPlayer())
			player1 = null;
		else if(player2 == event.getPlayer())
			player2 = null;
	}
	
	@EventHandler
	public void onSpawn(EntitySpawnEvent event) {
//		Bukkit.getLogger().info(event.getEntityType().toString());
//		Bukkit.getLogger().info(Integer.toString(event.getEntity().getEntityId()));
		
		if(event.getEntity() instanceof LivingEntity) {

			new BukkitRunnable() {
				Entity entity = event.getEntity();
				
				@Override
				public void run() {
					if(!gameStarted)
						return;
					else if(entity.isDead()) {
						this.cancel();
						return;
					}
					Location loc = entity.getLocation();
					
					if((loc.getX() > corners1.getKey().getX() &&
							loc.getY() > corners1.getKey().getY() &&
							loc.getZ() > corners1.getKey().getZ()) &&
							
							(loc.getX() < corners1.getValue().getX() &&
							loc.getY() < corners1.getValue().getY() &&
							loc.getZ() < corners1.getValue().getZ())) {
						points1 += PointsForPlayer(true, entity.getType());
						entity.remove();
						Bukkit.broadcastMessage(ChatColor.BOLD.toString() + ChatColor.RED.toString() + player1.getName() + ChatColor.RESET.toString() + " has " + ChatColor.AQUA.toString() + ChatColor.ITALIC.toString() + points1 + ChatColor.RESET.toString() + " Points");
						as1.setCustomName(player1.getName() + " (" + ChatColor.AQUA.toString() + ChatColor.ITALIC.toString() + Integer.toString(points1) + ChatColor.RESET.toString() + ")");
					}
					
					if((loc.getX() > corners2.getKey().getX() &&
							loc.getY() > corners2.getKey().getY() &&
							loc.getZ() > corners2.getKey().getZ()) &&
							
							(loc.getX() < corners2.getValue().getX() &&
							loc.getY() < corners2.getValue().getY() &&
							loc.getZ() < corners2.getValue().getZ())) {
						points2 += PointsForPlayer(false, entity.getType());
						entity.remove();
						Bukkit.broadcastMessage(ChatColor.BOLD.toString() + ChatColor.RED.toString() + player2.getName() + ChatColor.RESET.toString() + " has " + ChatColor.AQUA.toString() + ChatColor.ITALIC.toString() + points2 + ChatColor.RESET.toString() + " Points");
						as1.setCustomName(player2.getName() + " (" + ChatColor.AQUA.toString() + ChatColor.ITALIC.toString() + Integer.toString(points2) + ChatColor.RESET.toString() + ")");
					}
				}
			}.runTaskTimer(this, 0, 1);
		}
	}

	
	boolean gameStarted = false;
	
	Player player1;
	Player player2;
	
	ArmorStand as1;
	ArmorStand as2;
	
	int points1;
	int points2;
	
	AbstractMap.SimpleEntry<Location, Location> corners1;
	AbstractMap.SimpleEntry<Location, Location> corners2;
	
	HashSet<EntityType> entities1 = new HashSet<EntityType>();
	HashSet<EntityType> entities2 = new HashSet<EntityType>();
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("mhstart")) {
			gameStarted = true;
			World world = getServer().getWorld("world");
			
//			world.setGameRule(GameRule.DO_MOB_SPAWNING , true);
//			world.setGameRule(GameRule.DO_PATROL_SPAWNING , true);
//			world.setGameRule(GameRule.DO_TRADER_SPAWNING , true);
			world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE , true);
			world.setGameRule(GameRule.DO_WEATHER_CYCLE , true);
			world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN , true);
			
			player1.getInventory().clear();
			player2.getInventory().clear();
			
			player1.removePotionEffect(PotionEffectType.SLOW_DIGGING);
			player2.removePotionEffect(PotionEffectType.SLOW_DIGGING);
			
			player1.removePotionEffect(PotionEffectType.WEAKNESS);
			player2.removePotionEffect(PotionEffectType.WEAKNESS);
			
			player1.setHealth(0.0d);
			player2.setHealth(0.0d);
			
			new BukkitRunnable() {
				@Override
				public void run() {
					gameStarted = false;
					
					if(points1 > points2) {
						player1.sendTitle(ChatColor.BOLD.toString() + ChatColor.GOLD.toString() + player2.getName(), ChatColor.DARK_GREEN.toString() + "Won the game!", 13, 70, 20);
						player2.sendTitle(ChatColor.BOLD.toString() + ChatColor.GOLD.toString() + player2.getName(), ChatColor.DARK_GREEN.toString() + "Won the game!", 13, 70, 20);
						getServer().dispatchCommand(getServer().getConsoleSender(), "/title @a actionbar {\"text\":\"" + player1.getName() + "\",\"bold\":true,\"color\":\"gold\"}");
					}
					else if (points2 > points1){
						player1.sendTitle(ChatColor.BOLD.toString() + ChatColor.GOLD.toString() + player1.getName(), ChatColor.DARK_GREEN.toString() + "Won the game!", 13, 70, 20);
						player2.sendTitle(ChatColor.BOLD.toString() + ChatColor.GOLD.toString() + player1.getName(), ChatColor.DARK_GREEN.toString() + "Won the game!", 13, 70, 20);
						getServer().dispatchCommand(getServer().getConsoleSender(), "/title @a actionbar {\"text\":\"" + player2.getName() + "\",\"bold\":true,\"color\":\"gold\"}");
					}
					else {
						player1.sendTitle(ChatColor.BOLD.toString() + ChatColor.DARK_PURPLE.toString() + "☯", ChatColor.DARK_PURPLE.toString() + "DRAW", 13, 70, 20);
						player2.sendTitle(ChatColor.BOLD.toString() + ChatColor.DARK_PURPLE.toString() + "☯", ChatColor.DARK_PURPLE.toString() + "DRAW", 13, 70, 20);
					}
				}
			}.runTaskLater(this, 20*60*45);//runTaskLater(this, 20*60*45);
		}
		if(command.getName().equalsIgnoreCase("mhstop"))
			gameStarted = false;
		
		return true;
	}
	
	int PointsForPlayer(boolean player1, EntityType type) {
		int returnValue = Integer.MIN_VALUE;
		
		if(player1) {
			if(!(entities1.contains(type) || entities2.contains(type)))
				returnValue = 10;
			else if(entities1.contains(type) && entities2.contains(type))
				returnValue = 1;
			else if(entities2.contains(type))
				returnValue = 5;
			else if(entities1.contains(type))
				returnValue = 1;
			else
				Bukkit.getLogger().warning("error calculating pints for player 1");
			
			entities1.add(type);
		}
		else {
			if(!(entities1.contains(type) || entities2.contains(type)))
				returnValue = 10;
			else if(entities1.contains(type) && entities2.contains(type))
				returnValue = 1;
			else if(entities1.contains(type))
				returnValue = 5;
			else if(entities2.contains(type))
				returnValue = 1;
			else
				Bukkit.getLogger().warning("error calculating pints for player 1");
			
			entities2.add(type);
		}
		
		return returnValue;
	}
	
	AbstractMap.SimpleEntry<Location, Location> CreateBox(Location loc) {
		World world = getServer().getWorld("world");
		loc.setY(world.getHighestBlockYAt(loc) - 2);
		
		int x1 = loc.getBlockX() - 2;
		int y1 = loc.getBlockY() - 2;
		int z1 = loc.getBlockZ() - 2;
		
		int x2 = loc.getBlockX() + 2;
		int y2 = loc.getBlockY() + 2;
		int z2 = loc.getBlockZ() + 2;
		
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				for (int z = z1; z <= z2; z++) {
					if(y == y1)
						continue;
					Block block = world.getBlockAt(x, y, z);
					if(y > y1 + 1 && (x != x1 && x != x2) && (z != z1 && z != z2))
						block.setType(Material.AIR);
					else {
						block.setType(Material.JIGSAW);
						BlockData data = block.getBlockData();
						if(y == y1 + 1)
							((Jigsaw)data).setOrientation(Orientation.UP_NORTH);
						if(y > y1 + 1 && x == x1)
							((Jigsaw)data).setOrientation(Orientation.UP_WEST);
						if(y > y1 + 1 && x == x2)
							((Jigsaw)data).setOrientation(Orientation.UP_EAST);
						if(y > y1 + 1 && z == z1)
							((Jigsaw)data).setOrientation(Orientation.UP_NORTH);
						if(y > y1 + 1 && z == z2)
							((Jigsaw)data).setOrientation(Orientation.UP_SOUTH);
						block.setBlockData(data);
					}
				}
			}
		}
		return new AbstractMap.SimpleEntry<Location, Location>(new Location(world, x1, y1, z1), new Location(world, (float)x2, (float)y2 - 1.01, (float)z2));
	}
}
