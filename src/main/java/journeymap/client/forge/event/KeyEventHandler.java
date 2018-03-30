package journeymap.client.forge.event;

import javax.annotation.*;
import net.minecraft.client.settings.*;
import net.minecraft.client.*;
import org.apache.logging.log4j.*;
import net.minecraftforge.fml.client.*;
import journeymap.common.*;
import net.minecraftforge.client.settings.*;
import journeymap.client.ui.minimap.*;
import journeymap.client.ui.*;
import com.google.common.collect.*;
import journeymap.client.ui.waypoint.*;
import journeymap.client.api.display.*;
import journeymap.client.ui.component.*;
import journeymap.common.properties.*;
import journeymap.client.*;
import net.minecraftforge.fml.client.registry.*;
import journeymap.client.log.*;
import net.minecraftforge.fml.common.gameevent.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.client.event.*;
import journeymap.client.ui.dialog.*;
import org.lwjgl.input.*;
import java.util.function.*;
import java.util.stream.*;
import journeymap.common.log.*;
import java.util.*;
import journeymap.client.ui.fullscreen.*;

@ParametersAreNonnullByDefault
public enum KeyEventHandler implements EventHandlerManager.EventHandler
{
    INSTANCE;
    
    public KeyBinding kbMapZoomin;
    public KeyBinding kbMapZoomout;
    public KeyBinding kbMapToggleType;
    public KeyBinding kbCreateWaypoint;
    public KeyBinding kbFullscreenCreateWaypoint;
    public KeyBinding kbFullscreenChatPosition;
    public KeyBinding kbFullscreenToggle;
    public KeyBinding kbWaypointManager;
    public KeyBinding kbMinimapToggle;
    public KeyBinding kbMinimapPreset;
    public KeyBinding kbFullmapOptionsManager;
    public KeyBinding kbFullmapPanNorth;
    public KeyBinding kbFullmapPanSouth;
    public KeyBinding kbFullmapPanEast;
    public KeyBinding kbFullmapPanWest;
    private Comparator<KeyBindingAction> kbaComparator;
    private final ListMultimap<Integer, KeyBindingAction> minimapPreviewActions;
    private final ListMultimap<Integer, KeyBindingAction> inGameActions;
    private final ListMultimap<Integer, KeyBindingAction> inGuiActions;
    private Minecraft mc;
    private boolean sortActionsNeeded;
    private Logger logger;
    
    private KeyEventHandler() {
        this.kbaComparator = Comparator.comparingInt(KeyBindingAction::order);
        this.minimapPreviewActions = (ListMultimap<Integer, KeyBindingAction>)MultimapBuilder.hashKeys().arrayListValues(2).build();
        this.inGameActions = (ListMultimap<Integer, KeyBindingAction>)MultimapBuilder.hashKeys().arrayListValues(2).build();
        this.inGuiActions = (ListMultimap<Integer, KeyBindingAction>)MultimapBuilder.hashKeys().arrayListValues(2).build();
        this.mc = FMLClientHandler.instance().getClient();
        this.sortActionsNeeded = true;
        this.logger = Journeymap.getLogger();
        this.kbMapZoomin = this.register("key.journeymap.zoom_in", (IKeyConflictContext)KeyConflictContext.UNIVERSAL, KeyModifier.NONE, 13);
        this.setAction(this.minimapPreviewActions, this.kbMapZoomin, () -> MiniMap.state().zoomIn());
        this.setAction(this.inGuiActions, this.kbMapZoomin, () -> this.getFullscreen().zoomIn());
        this.kbMapZoomout = this.register("key.journeymap.zoom_out", (IKeyConflictContext)KeyConflictContext.UNIVERSAL, KeyModifier.NONE, 12);
        this.setAction(this.minimapPreviewActions, this.kbMapZoomout, () -> MiniMap.state().zoomOut());
        this.setAction(this.inGuiActions, this.kbMapZoomout, () -> this.getFullscreen().zoomOut());
        this.kbMapToggleType = this.register("key.journeymap.minimap_type", (IKeyConflictContext)KeyConflictContext.UNIVERSAL, KeyModifier.NONE, 26);
        this.setAction(this.minimapPreviewActions, this.kbMapToggleType, () -> MiniMap.state().toggleMapType());
        this.setAction(this.inGuiActions, this.kbMapToggleType, () -> this.getFullscreen().toggleMapType());
        this.kbMinimapPreset = this.register("key.journeymap.minimap_preset", (IKeyConflictContext)KeyConflictContext.IN_GAME, KeyModifier.NONE, 43);
        this.setAction(this.minimapPreviewActions, this.kbMinimapPreset, UIManager.INSTANCE::switchMiniMapPreset);
        this.inGameActions.putAll((Multimap)this.minimapPreviewActions);
        this.kbCreateWaypoint = this.register("key.journeymap.create_waypoint", (IKeyConflictContext)KeyConflictContext.IN_GAME, KeyModifier.NONE, 48);
        this.setAction(this.inGameActions, this.kbCreateWaypoint, WaypointEditor::openPlayerWaypoint);
        this.kbFullscreenCreateWaypoint = this.register("key.journeymap.fullscreen_create_waypoint", (IKeyConflictContext)KeyConflictContext.GUI, KeyModifier.NONE, 48);
        this.setAction(this.inGuiActions, this.kbFullscreenCreateWaypoint, () -> this.getFullscreen().createWaypointAtMouse());
        this.kbFullscreenChatPosition = this.register("key.journeymap.fullscreen_chat_position", (IKeyConflictContext)KeyConflictContext.GUI, KeyModifier.NONE, 46);
        this.setAction(this.inGuiActions, this.kbFullscreenChatPosition, () -> this.getFullscreen().chatPositionAtMouse());
        this.kbFullscreenToggle = this.register("key.journeymap.map_toggle_alt", (IKeyConflictContext)KeyConflictContext.UNIVERSAL, KeyModifier.NONE, 36);
        this.setAction(this.inGameActions, this.kbFullscreenToggle, UIManager.INSTANCE::openFullscreenMap);
        this.setAction(this.inGuiActions, this.kbFullscreenToggle, UIManager.INSTANCE::closeAll);
        this.kbWaypointManager = this.register("key.journeymap.fullscreen_waypoints", (IKeyConflictContext)KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, 48);
        this.setAction(this.inGameActions, this.kbWaypointManager, () -> UIManager.INSTANCE.openWaypointManager(null, null));
        this.setAction(this.inGuiActions, this.kbWaypointManager, () -> UIManager.INSTANCE.openWaypointManager(null, this.getFullscreen()));
        this.kbMinimapToggle = this.register("key.journeymap.minimap_toggle_alt", (IKeyConflictContext)KeyConflictContext.IN_GAME, KeyModifier.CONTROL, 36);
        this.setAction(this.inGameActions, this.kbMinimapToggle, UIManager.INSTANCE::toggleMinimap);
        this.kbFullmapOptionsManager = this.register("key.journeymap.fullscreen_options", (IKeyConflictContext)KeyConflictContext.GUI, KeyModifier.NONE, 24);
        this.setAction(this.inGuiActions, this.kbFullmapOptionsManager, () -> UIManager.INSTANCE.openOptionsManager(this.getFullscreen(), new Category[0]));
        this.kbFullmapPanNorth = this.register("key.journeymap.fullscreen.north", (IKeyConflictContext)KeyConflictContext.GUI, KeyModifier.NONE, 200);
        this.setAction(this.inGuiActions, this.kbFullmapPanNorth, () -> this.getFullscreen().moveCanvas(0, -16));
        this.kbFullmapPanSouth = this.register("key.journeymap.fullscreen.south", (IKeyConflictContext)KeyConflictContext.GUI, KeyModifier.NONE, 208);
        this.setAction(this.inGuiActions, this.kbFullmapPanSouth, () -> this.getFullscreen().moveCanvas(0, 16));
        this.kbFullmapPanEast = this.register("key.journeymap.fullscreen.east", (IKeyConflictContext)KeyConflictContext.GUI, KeyModifier.NONE, 205);
        this.setAction(this.inGuiActions, this.kbFullmapPanEast, () -> this.getFullscreen().moveCanvas(16, 0));
        this.kbFullmapPanWest = this.register("key.journeymap.fullscreen.west", (IKeyConflictContext)KeyConflictContext.GUI, KeyModifier.NONE, 203);
        this.setAction(this.inGuiActions, this.kbFullmapPanWest, () -> this.getFullscreen().moveCanvas(-16, 0));
    }
    
    private void setAction(final ListMultimap<Integer, KeyBindingAction> multimap, final KeyBinding keyBinding, final Runnable action) {
        multimap.put((Object)keyBinding.func_151463_i(), (Object)new KeyBindingAction(keyBinding, action));
    }
    
    private KeyBinding register(final String description, final IKeyConflictContext keyConflictContext, final KeyModifier keyModifier, final int keyCode) {
        final String category = (keyConflictContext == KeyConflictContext.GUI) ? Constants.getString("jm.common.hotkeys_keybinding_fullscreen_category") : Constants.getString("jm.common.hotkeys_keybinding_category");
        final KeyBinding kb = new UpdateAwareKeyBinding(description, keyConflictContext, keyModifier, keyCode, category);
        try {
            ClientRegistry.registerKeyBinding(kb);
        }
        catch (Throwable t) {
            ChatLog.announceError("Unexpected error when registering keybinding : " + kb);
        }
        return kb;
    }
    
    @SubscribeEvent
    public void onGameKeyboardEvent(final InputEvent.KeyInputEvent event) {
        final int key = Keyboard.getEventKey();
        if (Keyboard.isKeyDown(key)) {
            this.onInputEvent((Multimap<Integer, KeyBindingAction>)this.inGameActions, key, true);
        }
    }
    
    @SubscribeEvent
    public void onGuiKeyboardEvent(final GuiScreenEvent.KeyboardInputEvent.Post event) {
        final int key = Keyboard.getEventKey();
        if (Keyboard.isKeyDown(key)) {
            if (this.inFullscreenWithoutChat()) {
                this.onInputEvent((Multimap<Integer, KeyBindingAction>)this.inGuiActions, key, true);
            }
            else if (this.inMinimapPreview() && this.onInputEvent((Multimap<Integer, KeyBindingAction>)this.minimapPreviewActions, key, false)) {
                ((OptionsManager)this.mc.field_71462_r).refreshMinimapOptions();
            }
        }
    }
    
    @SubscribeEvent
    public void onGuiMouseEvent(final GuiScreenEvent.MouseInputEvent.Post event) {
        final int key = -100 + Mouse.getEventButton();
        if (!Mouse.isButtonDown(key)) {
            if (this.inFullscreenWithoutChat()) {
                this.onInputEvent((Multimap<Integer, KeyBindingAction>)this.inGuiActions, key, true);
            }
            else if (this.inMinimapPreview() && this.onInputEvent((Multimap<Integer, KeyBindingAction>)this.minimapPreviewActions, key, false)) {
                ((OptionsManager)this.mc.field_71462_r).refreshMinimapOptions();
            }
        }
    }
    
    public List<KeyBinding> getInGuiKeybindings() {
        final List<KeyBinding> list = this.inGuiActions.values().stream().map((Function<? super Object, ?>)KeyBindingAction::getKeyBinding).collect((Collector<? super Object, ?, List<KeyBinding>>)Collectors.toList());
        list.sort(Comparator.comparing(kb -> Constants.getString(kb.func_151464_g())));
        return list;
    }
    
    private boolean onInputEvent(final Multimap<Integer, KeyBindingAction> multimap, final int key, final boolean useContext) {
        try {
            if (this.sortActionsNeeded) {
                this.sortActions();
            }
            for (final KeyBindingAction kba : multimap.get((Object)key)) {
                if (kba.isActive(key, useContext)) {
                    this.logger.debug("Firing " + kba);
                    kba.getAction().run();
                    return true;
                }
            }
        }
        catch (Exception e) {
            this.logger.error("Error checking keybinding: " + LogFormatter.toPartialString(e));
        }
        return false;
    }
    
    private void sortActions() {
        this.sortActions(this.minimapPreviewActions);
        this.sortActions(this.inGameActions);
        this.sortActions(this.inGuiActions);
        this.sortActionsNeeded = false;
    }
    
    private void sortActions(final ListMultimap<Integer, KeyBindingAction> multimap) {
        final List<KeyBindingAction> copy = new ArrayList<KeyBindingAction>(multimap.values());
        multimap.clear();
        for (final KeyBindingAction kba : copy) {
            multimap.put((Object)kba.getKeyBinding().func_151463_i(), (Object)kba);
        }
        for (final Integer key : multimap.keySet()) {
            multimap.get((Object)key).sort(this.kbaComparator);
            Journeymap.getLogger().debug((Object)multimap.get((Object)key));
        }
    }
    
    private Fullscreen getFullscreen() {
        return UIManager.INSTANCE.openFullscreenMap();
    }
    
    private boolean inFullscreenWithoutChat() {
        return this.mc.field_71462_r instanceof Fullscreen && !((Fullscreen)this.mc.field_71462_r).isChatOpen();
    }
    
    private boolean inMinimapPreview() {
        return this.mc.field_71462_r instanceof OptionsManager && ((OptionsManager)this.mc.field_71462_r).previewMiniMap();
    }
    
    static class KeyBindingAction
    {
        KeyBinding keyBinding;
        Runnable action;
        
        public KeyBindingAction(final KeyBinding keyBinding, final Runnable action) {
            this.keyBinding = keyBinding;
            this.action = action;
        }
        
        boolean isActive(final int key, final boolean useContext) {
            if (useContext) {
                return this.keyBinding.isActiveAndMatches(key);
            }
            return this.keyBinding.func_151463_i() == key && this.keyBinding.getKeyModifier().isActive();
        }
        
        Runnable getAction() {
            return this.action;
        }
        
        KeyBinding getKeyBinding() {
            return this.keyBinding;
        }
        
        int order() {
            return this.keyBinding.getKeyModifier().ordinal();
        }
        
        @Override
        public String toString() {
            return "KeyBindingAction{" + this.keyBinding.getDisplayName() + " = " + Constants.getString(this.keyBinding.func_151464_g()) + '}';
        }
    }
    
    class UpdateAwareKeyBinding extends KeyBinding
    {
        UpdateAwareKeyBinding(final String description, final IKeyConflictContext keyConflictContext, final KeyModifier keyModifier, final int keyCode, final String category) {
            super(description, keyConflictContext, keyModifier, keyCode, category);
        }
        
        public void func_151462_b(final int keyCode) {
            super.func_151462_b(keyCode);
            KeyEventHandler.this.sortActionsNeeded = true;
        }
        
        public void setKeyModifierAndCode(final KeyModifier keyModifier, final int keyCode) {
            super.setKeyModifierAndCode(keyModifier, keyCode);
            KeyEventHandler.this.sortActionsNeeded = true;
        }
    }
}
