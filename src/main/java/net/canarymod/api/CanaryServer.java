package net.canarymod.api;

import com.mojang.authlib.GameProfile;
import net.canarymod.Canary;
import net.canarymod.Main;
import net.canarymod.ToolBox;
import net.canarymod.api.chat.ChatComponent;
import net.canarymod.api.entity.living.humanoid.CanaryPlayer;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.entity.vehicle.CanaryCommandBlockMinecart;
import net.canarymod.api.gui.GUIControl;
import net.canarymod.api.inventory.CanaryItem;
import net.canarymod.api.inventory.recipes.CanaryRecipe;
import net.canarymod.api.inventory.recipes.CraftingRecipe;
import net.canarymod.api.inventory.recipes.Recipe;
import net.canarymod.api.inventory.recipes.ShapedRecipeHelper;
import net.canarymod.api.inventory.recipes.SmeltRecipe;
import net.canarymod.api.nbt.CanaryCompoundTag;
import net.canarymod.api.world.UnknownWorldException;
import net.canarymod.api.world.World;
import net.canarymod.api.world.WorldManager;
import net.canarymod.api.world.blocks.CanaryCommandBlock;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.chat.ReceiverType;
import net.canarymod.config.Configuration;
import net.canarymod.exceptions.InvalidInstanceException;
import net.canarymod.hook.command.ConsoleCommandHook;
import net.canarymod.hook.system.PermissionCheckHook;
import net.canarymod.logger.Logman;
import net.canarymod.tasks.ServerTask;
import net.canarymod.tasks.ServerTaskManager;
import net.canarymod.util.NMSToolBox;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.gui.MinecraftServerGui;
import net.minecraft.server.management.ServerConfigurationManager;
import net.visualillusionsent.utils.TaskManager;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.canarymod.Canary.log;

/**
 * Main entry point of the software
 *
 * @author Jos Kuijpers
 * @author Chris (damagefilter)
 * @author Jason (darkdiplomat)
 */
public class CanaryServer implements Server {

    protected Map<String, ServerTimer> timers = new HashMap<String, ServerTimer>();
    private MinecraftServer server;
    private GUIControl currentGUI = null;
    String canaryVersion = null;
    private float tps = 20.0F; // Ticks Per Second Tracker

    /**
     * Create a new Server Wrapper
     *
     * @param server
     *         the MinecraftServer instance
     */
    public CanaryServer(MinecraftServer server) {
        this.server = server;
        addSynchronousTask(new TPSTracker(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
        }
        return "local.host";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumPlayersOnline() {
        return server.getConfigurationManager().getNumPlayersOnline();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxPlayers() {
        return Configuration.getServerConfig().getMaxPlayers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPlayerNameList() {
        List<Player> players = getPlayerList();
        String[] names = new String[players.size()];

        for (int i = 0; i < players.size(); i++) {
            names[i] = players.get(i).getName();
        }
        return names;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getKnownPlayerNames() {
        ArrayList<String> names = new ArrayList<String>();
        File playerDats = new File("worlds/players/");
        for (String name : playerDats.list()) {
            if (name.endsWith(".dat")) {
                names.add(name.substring(0, name.length() - ".dat".length()));
            }
        }
        return names.toArray(new String[names.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultWorldName() {
        return Configuration.getServerConfig().getDefaultWorldName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldManager getWorldManager() {
        return server.getWorldManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean consoleCommand(String command) {
        ConsoleCommandHook hook = (ConsoleCommandHook)new ConsoleCommandHook(this, command).call();
        if (hook.isCanceled()) {
            return true;
        }
        String[] args = command.split(" ");
        String cmdName = args[0];
        if (cmdName.startsWith("/")) {
            cmdName = cmdName.substring(1);
        }
        if (!Canary.commands().parseCommand(this, cmdName, args)) {
            return server.O().a(server, command) > 0; // Vanilla Commands passed
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean consoleCommand(String command, Player player) {
        ConsoleCommandHook hook = (ConsoleCommandHook)new ConsoleCommandHook(player, command).call();
        if (hook.isCanceled()) {
            return true;
        }
        String[] args = command.split(" ");
        String cmdName = args[0];
        if (cmdName.startsWith("/")) {
            cmdName = cmdName.substring(1);
        }
        if (!Canary.commands().parseCommand(player, cmdName, args)) {
            if (Canary.ops().isOpped(player.getName()) || player.hasPermission("canary.vanilla.".concat(cmdName))) {
                return server.O().a(((CanaryPlayer)player).getHandle(), command) > 0; // Vanilla Commands passed
            }
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean consoleCommand(String command, CommandBlockLogic cmdBlockLogic) {
        ConsoleCommandHook hook = new ConsoleCommandHook(cmdBlockLogic, command);

        hook.call();
        if (hook.isCanceled()) {
            return true;
        }
        String[] args = command.split(" ");
        String cmdName = args[0];
        if (cmdName.startsWith("/")) {
            cmdName = cmdName.substring(1);
        }

        // Don't pass off to Vanilla as that is already handled in NMS.CommandBlockLogic
        // Means, this is only called in CommandBlockLogic, if NMS didn't do anything with the command.
        return Canary.commands().parseCommand(cmdBlockLogic, cmdName, args);
    }

    @Override
    public void executeVanillaCommand(MessageReceiver caller, String command) {
        ICommandSender sender;
        switch (caller.getReceiverType()) {
            case PLAYER:
                sender = ((CanaryPlayer)caller).getHandle();
                break;
            case COMMANDBLOCK:
                sender = ((CanaryCommandBlock)caller).getLogic();
                break;
            case COMMANDBLOCKENTITY:
                sender = ((CanaryCommandBlockMinecart)caller).getLogic();
                break;
            case SERVER:
            default:
                sender = this.server;
        }
        server.O().a(sender, command);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimer(String uniqueName, int time) {
        if (timers.containsKey(uniqueName)) {
            log.warn("Unique key timer " + uniqueName + " is already running, skipping.");
            return;
        }
        ServerTimer newTimer = new ServerTimer(uniqueName);
        TaskManager.scheduleDelayedTaskInSeconds(newTimer, time);
        timers.put(uniqueName, newTimer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTimerExpired(String uniqueName) {
        return !timers.containsKey(uniqueName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player matchPlayer(String name) {
        Player lastPlayer = null;

        name = name.toLowerCase();

        for (Player cPlayer : server.getConfigurationManager().getAllPlayers()) {
            if (cPlayer.getName().toLowerCase().equals(name)) {
                // Perfect match found
                lastPlayer = cPlayer;
                break;
            }
            if (cPlayer.getName().toLowerCase().indexOf(name) != -1) {
                // Partial match
                if (lastPlayer != null) {
                    // Found multiple
                    return null;
                }
                lastPlayer = cPlayer;
            }
        }

        return lastPlayer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OfflinePlayer getOfflinePlayer(String player) {
        UUID uuid = ToolBox.uuidFromUsername(player);
        if (uuid == null) {
            return null;
        }
        return getOfflinePlayer(uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OfflinePlayer getOfflinePlayer(UUID uuid) {
        NBTTagCompound nbttagcompound = ServerConfigurationManager.getPlayerDat(uuid);
        CanaryCompoundTag comp;
        if (nbttagcompound != null) {
            comp = new CanaryCompoundTag(nbttagcompound);
            GameProfile profile = server.aD().a(uuid);
            if (profile != null) {
                return new CanaryOfflinePlayer(profile.getName(), uuid, comp);
            }
            else {
                String name = NMSToolBox.usernameFromUUID(uuid);
                if (name != null) {
                    return new CanaryOfflinePlayer(name, uuid, comp);
                }
                return new CanaryOfflinePlayer("PLAYER_NAME_UNKNOWN", uuid, comp);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerReference matchKnownPlayer(String player) {
        PlayerReference reference = matchPlayer(player);
        if (reference == null) {
            reference = getOfflinePlayer(player);
        }
        return reference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerReference matchKnownPlayer(UUID uuid) {
        PlayerReference reference = getPlayerFromUUID(uuid);
        if (reference == null) {
            reference = getOfflinePlayer(uuid);
        }
        return reference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getPlayer(String name) {
        return server.getConfigurationManager().getPlayerByName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getPlayerFromUUID(String uuid) {
        Player player = null;

        for (Player p : server.getConfigurationManager().getAllPlayers()) {
            if (p.getUUIDString().equals(uuid)) {
                player = p;
                break;
            }
        }

        return player;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getPlayerFromUUID(UUID uuid) {
        Player player = null;

        for (Player p : server.getConfigurationManager().getAllPlayers()) {
            if (p.getUUID().equals(uuid)) {
                player = p;
                break;
            }
        }

        return player;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Player> getPlayerList() {
        return server.getConfigurationManager().getAllPlayers();
    }

    public MinecraftServer getHandle() {
        return server;
    }

    @Override
    public void broadcastMessage(String message) {
        for (Player player : getPlayerList()) {
            player.message(message);
        }
        Canary.log.info(message);
    }

    @Override
    public void broadcastMessageToOps(String message) {
        for (Player player : getPlayerList()) {
            if (Canary.ops().isOpped(player)) {
                player.message(message);
            }
        }
        Canary.log.info(message);
    }

    @Override
    public void broadcastMessageToAdmins(String message) {
        for (Player player : getPlayerList()) {
            if (player.isAdmin()) {
                player.message(message);
            }
        }
        Canary.log.info(message);
    }

    @Override
    public boolean loadWorld(String name, long seed) {
        server.loadWorld(name, seed);
        if (server.getWorldManager().worldIsLoaded(name)) {
            return true;
        }
        return false;
    }

    @Override
    public World getWorld(String name) {
        try {
            return server.getWorldManager().getWorld(name, false);
        }
        catch (UnknownWorldException ukwex) {
            // @return {@link World} if found; {@code null} if not
            return null;
        }
    }

    @Override
    public World getDefaultWorld() {
        return getWorldManager().getWorld(getDefaultWorldName(), true);
    }

    @Override
    public ConfigurationManager getConfigurationManager() {
        return server.getConfigurationManager();
    }

    @Override
    public String getName() {
        return "Console";
    }

    @Override
    public boolean hasPermission(String node) {
        PermissionCheckHook hook = new PermissionCheckHook(node, this, true);
        Canary.hooks().callHook(hook);
        return hook.getResult();
    }

    @Override
    public boolean safeHasPermission(String node) {
        return true;
    }

    @Override
    public ReceiverType getReceiverType() {
        return ReceiverType.SERVER;
    }

    @Override
    public Player asPlayer() {
        throw new InvalidInstanceException("Server is not a MessageReceiver of the type: PLAYER");
    }

    @Override
    public Server asServer() {
        return this;
    }

    @Override
    public CommandBlockLogic asCommandBlock() {
        throw new InvalidInstanceException("Server is not a MessageReceiver of the type: COMMANDBLOCK");
    }

    @Override
    public String getLocale() {
        return Configuration.getServerConfig().getServerLocale();
    }

    @Override
    public void initiateShutdown(String message) {
        server.initShutdown(message);
    }

    @Override
    public void restart(boolean reloadCanary) {
        Main.restart(reloadCanary);
    }

    @Override
    public boolean isRunning() {
        return server.isRunning();
    }

    /**
     * Null the server reference
     */
    public void nullServer() {
        server = null;
    }

    @Override
    public void message(CharSequence message) {
        log.info(Logman.MESSAGE, message);
    }

    @Override
    public void message(String message) {
        log.info(Logman.MESSAGE, message);
    }

    @Override
    public void message(CharSequence... messages) {
        for (CharSequence message : messages) {
            message(message);
        }
    }

    @Override
    public void message(Iterable<? extends CharSequence> messages) {
        for (CharSequence message : messages) {
            message(message);
        }
    }

    @Override
    public void message(ChatComponent... chatComponents) {
        for(ChatComponent chatComponent : chatComponents){
            log.info(Logman.MESSAGE, chatComponent.getFullText());
        }
    }

    @Override
    public void notice(CharSequence message) {
        log.info(Logman.NOTICE, message);
    }

    @Override
    public void notice(String message) {
        log.info(Logman.NOTICE, message);
    }

    @Override
    public void notice(CharSequence... messages) {
        for (CharSequence message : messages) {
            notice(message);
        }
    }

    @Override
    public void notice(Iterable<? extends CharSequence> messages) {
        for (CharSequence message : messages) {
            notice(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Recipe addRecipe(CraftingRecipe recipe) {
        if (recipe.hasShape()) {
            return CraftingManager.a().a(((CanaryItem)recipe.getResult()).getHandle(), ShapedRecipeHelper.createRecipeShape(recipe)).getCanaryRecipe();
        }
        else {
            ItemStack result = ((CanaryItem)recipe.getResult()).getHandle();
            Object[] rec = new Object[recipe.getItems().length];

            for (int index = 0; index < recipe.getItems().length; index++) {
                rec[index] = ((CanaryItem)recipe.getItems()[index]).getHandle();
            }
            return CraftingManager.a().addShapeless(result, rec).getCanaryRecipe();
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Recipe> getServerRecipes() {
        List<IRecipe> server_recipes = CraftingManager.a().b();
        List<Recipe> rtn_recipes = new ArrayList<Recipe>();
        for (IRecipe recipe : server_recipes) {
            if (recipe instanceof ShapedRecipes) {
                rtn_recipes.add(((ShapedRecipes)recipe).getCanaryRecipe());
            }
            else if (recipe instanceof ShapelessRecipes) {
                rtn_recipes.add(((ShapelessRecipes)recipe).getCanaryRecipe());
            }
            // if it's neither, something went wrong or its something I haven't included yet
        }
        return rtn_recipes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeRecipe(Recipe recipe) {
        return CraftingManager.a().b().remove(((CanaryRecipe)recipe).getHandle());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSmeltingRecipe(SmeltRecipe recipe) {
        FurnaceRecipes.a().a(net.minecraft.item.Item.b(recipe.getItemIDFrom()), ((CanaryItem)recipe.getResult()).getHandle(), recipe.getXP());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SmeltRecipe> getServerSmeltRecipes() {
        return FurnaceRecipes.a().getSmeltingRecipes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeSmeltRecipe(SmeltRecipe recipe) {
        return FurnaceRecipes.a().removeSmeltingRecipe(recipe);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addGUI(GUIControl gui) {
        if (currentGUI != null) {
            currentGUI.closeWindow();
        }
        if (!isHeadless()) {
            currentGUI = gui;
            currentGUI.start();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long[] getTickTimeArray() {
        return server.g;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCanaryModVersion() {
        if (canaryVersion == null) {
            Package p = getClass().getPackage();
            if (p == null) {
                return "info missing!";
            }
            canaryVersion = p.getImplementationVersion();
        }
        return canaryVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServerVersion() {
        return server.F();
    }

    public int getProtocolVersion() {
        return server.getProtocolVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServerGUILog() {
        if (!isHeadless()) {
            return MinecraftServerGui.getLog();
        }
        else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GUIControl getCurrentGUI() {
        return this.currentGUI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHeadless() {
        return MinecraftServer.isHeadless();
    }

    public void setCurrentGUI(GUIControl guicontrol) {
        this.currentGUI = guicontrol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addSynchronousTask(ServerTask task) {
        return ServerTaskManager.addTask(task);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeSynchronousTask(ServerTask task) {
        return ServerTaskManager.removeTask(task);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPlayerListEntry(PlayerListEntry entry) {
        if (Configuration.getServerConfig().isPlayerListEnabled()) {
            // FIXME
            //server.ah().a(new S38PacketPlayerListItem(entry.getName(), entry.isShown(), entry.getPing()));
        }
    }

    @Override
    public void sendPlayerListData(PlayerListData data) {
        if (Configuration.getServerConfig().isPlayerListEnabled()) {
            server.an().a(new S38PacketPlayerListItem(data.getAction(), data));
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int getCurrentTick() {
        return server.ar();
    }

    public GameProfile gameprofileFromCache(UUID uuid) {
        return server.aD().a(uuid);
    }

    public GameProfile gameprofileFromCache(String username) {
        return server.aD().a(username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getTicksPerSecond() {
        return tps;
    }

    @Override
    public void showTitle(ChatComponent title) {
        showTitle(title, null);
    }

    @Override
    public void showTitle(ChatComponent title, ChatComponent subtitle) {
        for(Player player : getPlayerList()){
            player.showTitle(title, subtitle);
        }
    }

    public class ServerTimer implements Runnable {
        private String name;

        public ServerTimer(String name) {
            this.name = name;
        }

        @Override
        public synchronized void run() {
            timers.remove(name);
        }
    }

    /**
     * The internal CanaryServer Tick monitor task.
     * Used to track ticks per second.
     *
     * @author Jason (darkdiplomat)
     */
    private final class TPSTracker extends ServerTask {
        private long tpsSpan = System.currentTimeMillis();
        private int startTick = getCurrentTick();

        private TPSTracker(CanaryServer server) {
            super(server, 20L, true); // Run once every 20 ticks
        }

        @Override
        public final void onReset() {
            this.tpsSpan = System.currentTimeMillis();
            this.startTick = getCurrentTick();
        }

        @Override
        public final void run() {
            long timeSpan = System.currentTimeMillis() - tpsSpan;
            int ticks = getCurrentTick() - startTick;
            tps = (float)ticks / ((float)timeSpan / 1000.0F);
        }
    }
}
