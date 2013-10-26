package com.sky8the2flies.signenchant;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import com.sky8the2flies.util.SettingsManager;
import java.math.BigDecimal;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SignEnchant extends JavaPlugin implements Listener {
	public static String signName = "";
	public static String signAlias = "";
	public static String signBottom = "";
	public static String nFunds = "";
	public static String prefix = "";
	public static String nValid = "";
	public static String sValid = "";

	public static boolean log = false;
	public static boolean useTokens = false;

	SettingsManager settings = SettingsManager.getInstance();

	public void onEnable() {
		this.settings.setup(this);
		getServer().getPluginManager().registerEvents(this, this);
		getConfig().options().copyDefaults(true);
		saveConfig();
		signName = colorize(getConfig().getString("signName"));
		signAlias = colorize(getConfig().getString("signAlias"));
		signBottom = colorize(getConfig().getString("signBottom"));
		nFunds = colorize(getConfig().getString("notEnoughFunds"));
		nValid = colorize(getConfig().getString("itemNotValid"));
		prefix = colorize(getConfig().getString("prefix"));
		sValid = colorize(getConfig().getString("enchantmentNotValid"));
		log = getConfig().getBoolean("logCommands");
		useTokens = getConfig().getBoolean("useTokenSystem");
		System.out.print("-----------------------------------------");
		System.out.print(" ");
		System.out.print("[SEnchant] Permissions system || Loaded - ");
		System.out.print("    - signenchant.create");
		System.out.print("    - signenchant.add ");
		System.out.print("    - signenchant.remove  ");
		System.out.print("    - signenchant.check");
		System.out.print("    - signenchant.use ");
		System.out.print("    - signenchant.help");
		System.out.print(" ");
		System.out.print("[SEnchant] Main debugging information - ");
		System.out.print("    - logCommands: " + log);
		System.out.print("    - useTokensSystem: " + useTokens);
		System.out.print(" ");
		System.out.print("-----------------------------------------");
	}

	@EventHandler
	public void onSignChangeEvent(SignChangeEvent e) {
		if ((e.getPlayer().hasPermission("senchant.create"))
				&& (e.getLine(0).equals(signAlias))) {
			if (e.getLine(1).contains(":")) {
				if (e.getLine(2).contains("$")) {
					e.setLine(0, signName);
					e.setLine(3, signBottom);
					e.getPlayer()
							.sendMessage(
									prefix
											+ " §aSuccesfully created an 'SEnchant' sign!");
					Location location = e.getBlock().getLocation();
					if (log) {
						System.out.print("[SEnchant] "
								+ e.getPlayer().getName()
								+ " created an 'SEnchant' sign at -");
						System.out.print("[SEnchant] x: " + location.getX());
						System.out.print("[SEnchant] y: " + location.getY());
						System.out.print("[SEnchant] z: " + location.getZ());
					}
				} else {
					e.getPlayer()
							.sendMessage(
									prefix
											+ " §cFailed to create the 'SEnchant' sign. (3rd line)");
				}
			} else {
				if ((e.getLine(0).equals(signAlias))
						&& (e.getLine(1).equals(""))) {
					e.getPlayer()
							.sendMessage(
									prefix
											+ " §cSign is blank, creating script errors.");
				} else {
					e.getPlayer()
							.sendMessage(
									prefix
											+ " §cFailed to create the 'SEnchant' sign. (2nd line)");
					return;
				}
				return;
			}
			return;
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent e) {
		if ((!e.getPlayer().hasPermission("senchant.use"))
				&& (e.getAction() == Action.RIGHT_CLICK_BLOCK)
				&& ((e.getClickedBlock().getType() == Material.SIGN_POST) || (e
						.getClickedBlock().getType() == Material.WALL_SIGN))) {
			Sign sign = (Sign) e.getClickedBlock().getState();
			if (sign.getLine(0).equals(signName)) {
				e.getPlayer()
						.sendMessage(
								prefix
										+ " §cYou must be a donator to use 'SEnchant' sign's");
			}
		}

		if (e.getPlayer().hasPermission("senchant.use"))
			try {
				if ((e.getAction() == Action.RIGHT_CLICK_BLOCK)
						&& ((e.getClickedBlock().getType() == Material.SIGN_POST) || (e
								.getClickedBlock().getType() == Material.WALL_SIGN))) {
					Sign sign = (Sign) e.getClickedBlock().getState();
					if (sign.getLine(0).equals(signName)) {
						String enchantmentName = nameToEnchantment(sign
								.getLine(1).substring(0,
										sign.getLine(1).indexOf(":")));
						int enchantmentLevel = Integer.parseInt(sign.getLine(1)
								.substring(sign.getLine(1).indexOf(":"))
								.replace(':', ' ').trim());
						Enchantment ench = Enchantment
								.getByName(enchantmentName);
						String line = sign.getLine(1);
						String[] split = line.split(":");
						int enchLevel = Integer.parseInt(split[1]);
						int level = e.getPlayer().getItemInHand()
								.getEnchantmentLevel(ench);
						try {
							if ((e.getPlayer().getItemInHand()
									.containsEnchantment(ench))
									&& (enchLevel == level)) {
								e.getPlayer()
										.sendMessage(
												prefix
														+ " §cThe item you are holding already has that enchantment.");
							} else {
								if (Economy
										.getMoneyExact(e.getPlayer().getName())
										.subtract(
												new BigDecimal(
														Integer.parseInt(sign
																.getLine(2)
																.substring(1))))
										.intValue() <= 0) {
									e.getPlayer().sendMessage(
											prefix + " " + nFunds);
									return;
								}

								if (!getConfig().getList("validEnchants")
										.contains(
												sign.getLine(1).substring(
														0,
														sign.getLine(1)
																.indexOf(":")))) {
									e.getPlayer().sendMessage(
											prefix + " " + sValid);
									return;
								}
								if (e.getPlayer().getItemInHand() == null) {
									e.getPlayer().sendMessage(
											prefix + " " + nValid);
									return;
								}

								if (!getConfig().getList("validItems")
										.contains(
												Integer.valueOf(e.getPlayer()
														.getItemInHand()
														.getTypeId()))) {
									e.getPlayer().sendMessage(
											prefix + " " + nValid);
									return;
								}
								if (useTokens) {
									if (this.settings.getTokens()
											.getInt(e.getPlayer().getName()
													+ ".tokens") <= 0) {
										e.getPlayer()
												.sendMessage(
														prefix
																+ " §cYou have no tokens to spend on this enchant!");
										return;
									}
									return;
								}
								Economy.setMoney(
										e.getPlayer().getName(),
										Economy.getMoneyExact(
												e.getPlayer().getName())
												.subtract(
														new BigDecimal(
																Integer.parseInt(sign
																		.getLine(
																				2)
																		.substring(
																				1)))));
								if (useTokens) {
									this.settings
											.getTokens()
											.set(e.getPlayer().getName()
													+ ".tokens",
													Integer.valueOf(this.settings
															.getTokens()
															.getInt(e
																	.getPlayer()
																	.getName()
																	+ ".tokens") - 1));
								}

								e.getPlayer()
										.getItemInHand()
										.addUnsafeEnchantment(ench,
												enchantmentLevel);
								e.getPlayer().sendMessage(
										prefix
												+ " You received "
												+ sign.getLine(1).substring(
														0,
														sign.getLine(1)
																.indexOf(":"))
												+ ":" + enchantmentLevel
												+ " for " + sign.getLine(2)
												+ ".");
								if (log) {
									System.out.println("[SEnchant] "
											+ e.getPlayer().getName()
											+ " purchased " + sign.getLine(1)
											+ ":" + " for " + sign.getLine(2));
								}
							}
						} catch (NumberFormatException e2) {
							e.getPlayer()
									.sendMessage("Sign format 'protected'");
						} catch (NoLoanPermittedException e1) {
							e.getPlayer().sendMessage(prefix + " " + nFunds);
						} catch (IndexOutOfBoundsException localIndexOutOfBoundsException) {
						} catch (UserDoesNotExistException localUserDoesNotExistException) {
						}
					}
				}
			} catch (StringIndexOutOfBoundsException el) {
				e.getPlayer().sendMessage(
						prefix + " §cUnable to use the 'SEnchant' sign.");
			}
	}

	public String colorize(String Message) {
		return Message.replaceAll("~([a-z0-9])", "§$1");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (cmd.getName().equalsIgnoreCase("tokens")) {
			if (!sender.hasPermission("senchant.main")) {
				sender.sendMessage(prefix
						+ " §cYou do not have permissions for this command!");
			}
			if (args.length <= 0) {
				showHelp(0, sender);
			}

			Player player = (Player) sender;

			if (args.length > 0) {
				String sec = args[0];
				if (sec.equalsIgnoreCase("help")) {
					if (!sender.hasPermission("senchant.help")) {
						sender.sendMessage(prefix
								+ " §cYou do not have permission for this command!");
					} else {
						showHelp(0, sender);
					}
					return true;
				}
				if (sec.equalsIgnoreCase("reset")) {
					try {
						this.settings.getTokens()
								.set(this.settings.getTokens().getName()
										+ ".tokens",
										this.settings.getTokens().getName()
												+ ".initialTokens");
					} catch (NullPointerException e) {
						e = null;
					}
				}
				if (sec.equalsIgnoreCase("add")) {
					if (args.length <= 2) {
						showHelp(1, sender);
					}
					if (!sender.hasPermission("senchant.add")) {
						sender.sendMessage(prefix
								+ " §cYou do not have permission for this command!");
					} else {
						if (sender.hasPermission("senchant.add")) {
							if (args.length == 3) {
								String playername = args[2];
								int amount = 0;
								try {
									amount = Integer.parseInt(args[1]);
								} catch (NumberFormatException e) {
									player.sendMessage(prefix
											+ " §cThe amount you entered is not a whole number");
								}
								try {
									Player giftedPlayer = getServer()
											.getPlayer(playername);
									this.settings.getTokens().set(
											giftedPlayer.getName() + ".tokens",
											Integer.valueOf(this.settings
													.getTokens()
													.getInt(giftedPlayer
															.getName()
															+ ".tokens")
													+ amount));
									this.settings.saveTokens();

									if (!this.settings.getTokens().contains(
											giftedPlayer.getName()
													+ ".initialTokens")) {
										this.settings.getTokens().set(
												giftedPlayer.getName()
														+ ".initialTokens",
												Integer.valueOf(amount));
										this.settings.saveTokens();
									}
									sender.sendMessage(prefix
											+ " §7You sent §6'" + amount
											+ "' §7token to §6" + playername
											+ "'s §7account.");
									giftedPlayer
											.sendMessage(prefix
													+ " §a'"
													+ amount
													+ "' §ftoken(s) have been added to your account.");
								} catch (NullPointerException e) {
									player.sendMessage(prefix + " §4"
											+ playername
											+ " §cis not a valid player.");
								}
								return true;
							}
							return true;
						}
						return true;
					}
					return true;
				}
				if (sec.equalsIgnoreCase("remove")) {
					if (!sender.hasPermission("senchant.remove")) {
						sender.sendMessage(prefix
								+ " §cYou do not have permission for this command!");
					} else {
						if (sender.hasPermission("senchant.remove")) {
							if (args.length == 3) {
								String playername = args[2];
								int amount = 0;
								try {
									amount = Integer.parseInt(args[1]);
								} catch (NumberFormatException e) {
									player.sendMessage(prefix
											+ " §cThe amount you entered is not a whole number");
								}
								try {
									Player giftedPlayer = getServer()
											.getPlayer(playername);
									this.settings.getTokens().set(
											giftedPlayer.getName() + ".tokens",
											Integer.valueOf(this.settings
													.getTokens()
													.getInt(giftedPlayer
															.getName()
															+ ".tokens")
													- amount));
									this.settings.saveTokens();
									sender.sendMessage(prefix
											+ " §cYou removed §4'" + amount
											+ "' §ctokens from §4" + playername
											+ "'s §caccount");
									giftedPlayer
											.sendMessage(prefix
													+ " §4'"
													+ amount
													+ "' §ctokens have been removed from your account!");
								} catch (NullPointerException e) {
									player.sendMessage(prefix + " §4"
											+ playername
											+ " §cis not a valid player.");
								}
								return true;
							}
							showHelp(2, sender);
							return true;
						}

						showHelp(0, sender);

						return true;
					}
					return true;
				}
				if (sec.equalsIgnoreCase("check")) {
					if (!sender.hasPermission("senchant.check"))
						sender.sendMessage(prefix
								+ " §cYou do not have permission for this command!");
					else {
						try {
							if (args.length == 2) {
								String playername = args[1];
								Player selectedPlayer = getServer().getPlayer(
										playername);
								sender.sendMessage(prefix
										+ " §a\""
										+ playername
										+ "'s\" §faccount currently has §a"
										+ this.settings.getTokens().getInt(
												new StringBuilder(String
														.valueOf(selectedPlayer
																.getName()))
														.append(".tokens")
														.toString())
										+ " §ftokens.");
								return true;
							}
							player.sendMessage(prefix
									+ " §fYou have §a"
									+ this.settings.getTokens().getInt(
											new StringBuilder(String
													.valueOf(player.getName()))
													.append(".tokens")
													.toString()) + " §ftokens.");
							return true;
						} catch (NullPointerException e) {
							sender.sendMessage(prefix
									+ " §cThat player is not a liable player");
						}
					}
					return true;
				}
				if (sec.equalsIgnoreCase("reload")) {
					if (player.hasPermission("senchant.reload")) {
						reloadConfig();
						player.sendMessage("Reloaded from disk!");
						return true;
					}
					return true;
				}
				showHelp(0, sender);
				return true;
			}

			return true;
		}
		return true;
	}

	public void showHelp(int menu, CommandSender sender) {
		switch (menu) {
		case 0:
			sender.sendMessage("§6SEnchant Help§7: §4<required> [optional]");
			sender.sendMessage("§6\"/tokens add <amount> <name>\" §7- Add tokens to selected users account.");
			sender.sendMessage("§6\"/tokens check [name]\" §7- Check the current amount of tokens from selected users account.");
			sender.sendMessage("§6\"/tokens remove <amount> <name>\" §7- Remove tokens from the selected users account.");
			sender.sendMessage("§7Written by sky8the2flies.");
			break;
		case 1:
			sender.sendMessage("§6Correct usage: \"/tokens add <name> <amount>\"");
			break;
		case 2:
			sender.sendMessage("§6Correct usage: \"/tokens remove <name> <amount>\"");
		}
	}

	public String nameToEnchantment(String originalEnchantmentName) {
		String newEnchantmentName = originalEnchantmentName;
		try {
			if (originalEnchantmentName.equalsIgnoreCase("power"))
				newEnchantmentName = "ARROW_DAMAGE";
			if (originalEnchantmentName.equalsIgnoreCase("flame"))
				newEnchantmentName = "ARROW_FIRE";
			if (originalEnchantmentName.equalsIgnoreCase("infinity"))
				newEnchantmentName = "ARROW_INFINITE";
			if (originalEnchantmentName.equalsIgnoreCase("punch"))
				newEnchantmentName = "ARROW_KNOCKBACK";
			if (originalEnchantmentName.equalsIgnoreCase("sharpness"))
				newEnchantmentName = "DAMAGE_ALL";
			if (originalEnchantmentName.equalsIgnoreCase("baneofa"))
				newEnchantmentName = "DAMAGE_ARTHROPODS";
			if (originalEnchantmentName.equalsIgnoreCase("smite"))
				newEnchantmentName = "DAMAGE_UNDEAD";
			if (originalEnchantmentName.equalsIgnoreCase("unbreaking"))
				newEnchantmentName = "DURABILITY";
			if (originalEnchantmentName.equalsIgnoreCase("fire_aspect"))
				newEnchantmentName = "FIRE_ASPECT";
			if (originalEnchantmentName.equalsIgnoreCase("knockback"))
				newEnchantmentName = "KNOCKBACK";
			if (originalEnchantmentName.equalsIgnoreCase("fortune"))
				newEnchantmentName = "LOOT_BONUS_BLOCKS";
			if (originalEnchantmentName.equalsIgnoreCase("looting"))
				newEnchantmentName = "LOOT_BONUS_MOBS";
			if (originalEnchantmentName.equalsIgnoreCase("respiration"))
				newEnchantmentName = "OXYGEN";
			if (originalEnchantmentName.equalsIgnoreCase("protection"))
				newEnchantmentName = "PROTECTION_ENVIRONMENTAL";
			if (originalEnchantmentName.equalsIgnoreCase("blastprot"))
				newEnchantmentName = "PROTECTION_EXPLOSIONS";
			if (originalEnchantmentName.equalsIgnoreCase("efficiency"))
				newEnchantmentName = "DIG_SPEED";
			if (originalEnchantmentName.equalsIgnoreCase("featherfall"))
				newEnchantmentName = "PROTECTION_FALL";
			if (originalEnchantmentName.equalsIgnoreCase("fireprot"))
				newEnchantmentName = "PROTECTION_FIRE";
			if (originalEnchantmentName.equalsIgnoreCase("arrowprot"))
				newEnchantmentName = "PROTECTION_PROJECTILE";
			if (originalEnchantmentName.equalsIgnoreCase("silk_touch"))
				newEnchantmentName = "SILK_TOUCH";
			if (originalEnchantmentName.equalsIgnoreCase("thorns"))
				newEnchantmentName = "THORNS";
			if (originalEnchantmentName.equalsIgnoreCase("aquafinity"))
				newEnchantmentName = "WATER_WORKER";
		} catch (IllegalArgumentException e) {
		}
		return newEnchantmentName;
	}
}