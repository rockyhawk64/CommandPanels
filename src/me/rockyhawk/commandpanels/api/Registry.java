package me.rockyhawk.commandpanels.api;

import me.rockyhawk.commandpanels.builder.inventory.items.ItemComponent;
import me.rockyhawk.commandpanels.builder.inventory.items.MaterialComponent;
import me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents.BannerComponent;
import me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents.CustomModelDataComponent;
import me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents.DamageComponent;
import me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents.EnchantedComponent;
import me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents.ItemModelComponent;
import me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents.LeatherColorComponent;
import me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents.PotionColorComponent;
import me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents.PotionComponent;
import me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents.StackComponent;
import me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents.TooltipComponent;
import me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents.TrimComponent;
import me.rockyhawk.commandpanels.builder.inventory.items.materialcomponents.HeadComponent;
import me.rockyhawk.commandpanels.builder.inventory.items.materialcomponents.HeadDatabaseComponent;
import me.rockyhawk.commandpanels.builder.inventory.items.materialcomponents.ItemsAdderComponent;
import me.rockyhawk.commandpanels.builder.inventory.items.materialcomponents.MMOItemsComponent;
import me.rockyhawk.commandpanels.builder.inventory.items.materialcomponents.MinecraftComponent;
import me.rockyhawk.commandpanels.builder.inventory.items.materialcomponents.NexoComponent;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagResolver;
import me.rockyhawk.commandpanels.interaction.commands.RequirementTagResolver;
import me.rockyhawk.commandpanels.interaction.commands.requirements.ConditionTag;
import me.rockyhawk.commandpanels.interaction.commands.requirements.ItemTag;
import me.rockyhawk.commandpanels.interaction.commands.requirements.VaultTag;
import me.rockyhawk.commandpanels.interaction.commands.requirements.XpTag;
import me.rockyhawk.commandpanels.interaction.commands.tags.ChatTag;
import me.rockyhawk.commandpanels.interaction.commands.tags.ClosePanelTag;
import me.rockyhawk.commandpanels.interaction.commands.tags.ConsoleCmdTag;
import me.rockyhawk.commandpanels.interaction.commands.tags.DataTag;
import me.rockyhawk.commandpanels.interaction.commands.tags.GiveTag;
import me.rockyhawk.commandpanels.interaction.commands.tags.GrantTag;
import me.rockyhawk.commandpanels.interaction.commands.tags.ItemActionTag;
import me.rockyhawk.commandpanels.interaction.commands.tags.MessageTag;
import me.rockyhawk.commandpanels.interaction.commands.tags.OpenPanelTag;
import me.rockyhawk.commandpanels.interaction.commands.tags.PreviousPanelTag;
import me.rockyhawk.commandpanels.interaction.commands.tags.RefreshPanelTag;
import me.rockyhawk.commandpanels.interaction.commands.tags.ServerTag;
import me.rockyhawk.commandpanels.interaction.commands.tags.SessionTag;
import me.rockyhawk.commandpanels.interaction.commands.tags.SoundTag;
import me.rockyhawk.commandpanels.interaction.commands.tags.StopSoundTag;
import me.rockyhawk.commandpanels.interaction.commands.tags.TeleportTag;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public final class Registry<T extends Registrable> implements Iterable<T> {

    private final @NonNull CopyOnWriteArrayList<T> internalList;

    private Registry(final @NonNull Collection<T> collection) {
        this.internalList = new CopyOnWriteArrayList<>(collection);
    }

    /**
     * Inserts a new element to this registry
     */
    public void insert(final @NonNull T registrable) {
        internalList.add(registrable);
    }

    /**
     * Inserts a new element to this registry before the specified {@code index}.
     */
    public void insert(final int index, final @NonNull T registrable) throws IndexOutOfBoundsException {
        internalList.add(index, registrable);
    }

    /**
     * Removes an element at specified {@code index} from this registry.
     */
    public @NonNull T remove(final int index) throws IndexOutOfBoundsException {
        return internalList.remove(index);
    }

    /**
     * Removes all elements matching specified {@code predicate} from this registry.
     */
    public boolean removeIf(final @NonNull Predicate<T> predicate) throws IndexOutOfBoundsException {
        return internalList.removeIf(predicate);
    }

    /**
     * Returns a read-only / unmodifiable list of all elements in the registry.
     */
    public @NonNull List<T> all() {
        return all(false);
    }

    /**
     * Returns a read-only / unmodifiable, or mutable list of all elements in the registry.
     */
    public @NonNull List<T> all(final boolean mutable) {
        return (mutable) ? internalList : Collections.unmodifiableList(internalList);
    }


    public static final @NonNull Registry<ItemComponent> ITEM_COMPONENTS = new Registry<>(List.of(
            new EnchantedComponent(),
            new ItemModelComponent(),
            new CustomModelDataComponent(),
            new TooltipComponent(),
            new BannerComponent(),
            new LeatherColorComponent(),
            new PotionComponent(),
            new PotionColorComponent(),
            new DamageComponent(),
            new TrimComponent(),
            new StackComponent()
    ));


    public static final @NonNull Registry<MaterialComponent> MATERIAL_COMPONENTS = new Registry<>(List.of(
            new MinecraftComponent(),
            new HeadComponent(),
            new NexoComponent(),
            new ItemsAdderComponent(),
            new MMOItemsComponent(),
            new HeadDatabaseComponent()
    ));


    public static final @NonNull Registry<CommandTagResolver> COMMAND_TAG_RESOLVERS = new Registry<>(List.of(
            new OpenPanelTag(),
            new RefreshPanelTag(),
            new PreviousPanelTag(),
            new ClosePanelTag(),
            new ConsoleCmdTag(),
            new SessionTag(),
            new DataTag(),
            new ChatTag(),
            new GrantTag(),
            new ServerTag(),
            new MessageTag(),
            new GiveTag(),
            new ItemActionTag(),
            new SoundTag(),
            new StopSoundTag(),
            new TeleportTag()
    ));


    public static final @NonNull Registry<RequirementTagResolver> REQUIREMENT_TAG_RESOLVERS = new Registry<>(List.of(
            new ConditionTag(),
            new VaultTag(),
            new ItemTag(),
            new me.rockyhawk.commandpanels.interaction.commands.requirements.DataTag(),
            new XpTag()
    ));

    @Override
    public @NonNull Iterator<T> iterator() {
        return internalList.iterator();
    }

}
