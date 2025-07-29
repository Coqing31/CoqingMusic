package com.coqing.coqingmusic.guis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.inventory.ItemStack;

import com.coqing.coqingmusic.CoqingMusic;
import com.coqing.coqingmusic.nbs.loaders.SongHeader;
import com.coqing.coqingmusic.nbs.loaders.SongLoader;
import com.coqing.coqingutils.ComponentUtils;
import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.raphimc.noteblocklib.format.SongFormat;

public class PlayGUI {
    private final Player player;
    private final ComponentUtils cmp;
    private final SongLoader loader;
    private final CoqingMusic plugin;

    private final Function<Integer, String> humanLength = (length) -> String.format("%02d:%02d:%02d",
            length / 3600,
            (length / 60) % 60, length % 60);
    private static final DecimalFormat format = new DecimalFormat("#.##");

    /**
     * Do NOT use this function. Instead, use the GUI factory.
     * 
     * @param player
     * @param cmp
     * @param loader
     */
    public PlayGUI(Player player, ComponentUtils cmp, SongLoader loader, CoqingMusic plugin) {
        this.player = player;
        this.cmp = cmp;
        this.loader = loader;
        this.plugin = plugin;
    }

    public void openGUI() {
        this.openGUI(plugin.getDataPath().resolve("songs"));
    }

    public void openGUI(Path path) {
        this.openGUI(path, 1);
    }

    private int displayPriority(SongFormat format) {
        return switch (format) {
            case NBS -> 1;
            case MIDI -> 2;
            case TXT -> 3;
            case FUTURE_CLIENT -> 4;
            case MCSP, MCSP2 -> 5;
        };
    }

    private ItemStack arrow(boolean forwards) {
        ItemStack arrow = new ItemStack(Material.ARROW);
        arrow.setData(DataComponentTypes.CUSTOM_NAME, cmp.formatMessage("<!i><#99d98c><b>Go <dir></b></#99d98c>",
                Placeholder.unparsed("dir", forwards ? "Forwards" : "Backwards")));
        return arrow;
    }

    private GuiItem guiFolderItem(Path path) {
        int amount = 0;
        try (Stream<Path> stream = Files.list(path)) {
            amount = stream.filter(file -> !Files.isDirectory(file)).toList().size();
        } catch (IOException ex) {
            amount = 0;
        }

        ItemStack item = new ItemStack(Material.LIME_SHULKER_BOX);
        item.setData(DataComponentTypes.CUSTOM_NAME, cmp.formatMessage("<!i><#99d98c><folder></#99d98c>",
                Placeholder.unparsed("folder", path.getFileName().toString())));
        ItemLore lore = ItemLore.lore()
                .addLine(cmp.formatMessage(
                        "<gray>-</gray> <#99d98c><b>File Amount:</b></#99d98c> <#99e2b4><amount></#99e2b4>",
                        Placeholder.unparsed("amount", String.valueOf(amount))))
                .build();

        ItemLore finalLore = ItemLore.lore(lore.lines().stream()
                .map(line -> line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)).toList());

        item.setData(DataComponentTypes.LORE, finalLore);
        return new GuiItem(item, event -> openGUI(path));

    }

    private GuiItem guiHeaderItem(SongHeader header) {
        Material material = switch (header.getFormat()) {
            case NBS -> Material.NOTE_BLOCK;
            case MIDI -> Material.JUKEBOX;
            case MCSP, MCSP2 -> Material.GRASS_BLOCK;
            case TXT -> Material.BOOK;
            case FUTURE_CLIENT -> Material.STONE_SWORD;
        };

        ItemStack item = new ItemStack(material);
        item.setData(DataComponentTypes.CUSTOM_NAME,
                cmp.formatMessage("<!i><#99d98c><b><file></b></#99d98c>",
                        Placeholder.unparsed("file", header.getSource().getFileName().toString())));
        List<String> lore = new ArrayList<>();
        lore.add("<gray>-</gray> <#99d98c><b>Title:</b></#99d98c> <#99e2b4><title></#99e2b4>");
        lore.add("<gray>-</gray> <#99d98c><b>Author:</b></#99d98c> <#99e2b4><author></#99e2b4>");
        lore.add("<gray>-</gray> <#99d98c><b>Original Author:</b></#99d98c> <#99e2b4><ogauthor></#99e2b4>");
        lore.add("<gray>-</gray> <#99d98c><b>Length:</b></#99d98c> <#99e2b4><length></#99e2b4>");
        lore.add("<gray>-</gray> <#99d98c><b>Tempo:</b></#99d98c> <#99e2b4><tempo></#99e2b4>");

        ItemLore.Builder itemlore = ItemLore.lore();
        float[] tempo = header.getTempo();
        String tpsString = tempo[0] != tempo[1]
                ? format.format(tempo[0]) + " - " + format.format(tempo[1]) + " TPS ("
                        + format.format(tempo[0] * 15) +
                        " - " + format.format(tempo[1] * 15) + " BPM)"
                : format.format(tempo[0]) + " TPS (" + format.format(tempo[0] * 15) + " BPM)";
        TagResolver[] resolvers = new TagResolver[] {
                Placeholder.unparsed("title", header.getTitle()),
                Placeholder.unparsed("author", header.getAuthor()),
                Placeholder.unparsed("ogauthor", header.getOriginalAuthor()),
                Placeholder.unparsed("length", humanLength.apply(header.getLength())),
                Placeholder.unparsed("tempo", tpsString)
        };
        lore.forEach(a -> itemlore.addLine(cmp.formatMessage(a, resolvers)));

        if (!header.getDescription().equalsIgnoreCase("Unset")) {
            itemlore.addLine(cmp.formatMessage("<gray>" + ("=".repeat(50) + "</gray>")));
            Arrays.asList(WordUtils.wrap(header.getDescription(), 50).split("[\\n\\r]+")).forEach(line -> {
                itemlore.addLine(
                        cmp.formatMessage("<#99e2b4><line></#99e2b4>", Placeholder.unparsed("line", line)));
            });
        }
        itemlore.addLine(cmp.formatMessage("<gray>" + ("=".repeat(50) + "</gray>")));
        itemlore.addLine(cmp.formatMessage("<dark_gray>(Click to play this song.)</dark_gray>"));

        // Remove italic formatting
        ItemLore finalLore = ItemLore.lore(itemlore.build().lines().stream()
                .map(line -> line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)).toList());

        item.setData(DataComponentTypes.LORE, finalLore);

        return new GuiItem(item, event -> {
            player.closeInventory(Reason.PLUGIN);
            this.loader.playSong(header);
        });

    }

    public void openGUI(Path path, int page) {
        String dp = path.toString().replace("plugins/CoqingMusic/songs", "");
        TagResolver pathDisplay = Placeholder.unparsed("path", dp.isBlank() ? "" : " - " + dp);
        ChestGui gui = new ChestGui(6, ComponentHolder
                .of(cmp.formatMessage("<prefix> <#99d98c>Song Browser<path></#99d98c>", pathDisplay)));

        gui.setOnGlobalClick(event -> event.setCancelled(true));

        PaginatedPane paginatedPane = new PaginatedPane(1, 1, 7, 4);

        List<Path> files = Arrays.stream(path.toFile().listFiles()).map(File::toPath).toList();
        List<GuiItem> allItems = new ArrayList<>();
        // Add dirs first
        allItems.addAll(files.stream().filter(Files::isDirectory).sorted().map(this::guiFolderItem).toList());
        allItems.addAll(files.stream().filter(p -> !Files.isDirectory(p)).map(p -> {
            Optional<SongHeader> header = this.loader.getLoadedSongs()
                    .keySet().stream()
                    .filter(h -> h.getSource().equals(p))
                    .findFirst();
            if (header.isPresent())
                return header.get();
            else
                return null;
        }).sorted(Comparator.comparingInt(h -> this.displayPriority(h.getFormat())))
                .map(this::guiHeaderItem).toList());

        paginatedPane.populateWithGuiItems(allItems);

        gui.addPane(paginatedPane);

        StaticPane borderPane = new StaticPane(0, 0, 9, 6);
        ItemStack limePane = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        limePane.setData(DataComponentTypes.HIDE_TOOLTIP);
        // Green panes
        List.of(0, 2, 4, 6, 8, 18, 26, 27, 35, 45, 47, 51, 53)
                .forEach(entry -> {
                    int index = (int) entry;
                    borderPane.addItem(new GuiItem(limePane),
                            Slot.fromIndex(index));
                });

        // Yellow panes
        ItemStack yellowPane = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        yellowPane.setData(DataComponentTypes.HIDE_TOOLTIP);
        List.of(1, 3, 5, 7, 9, 17, 36, 44, 46, 52).forEach(entry -> {
            int index = (int) entry;
            borderPane.addItem(new GuiItem(yellowPane), Slot.fromIndex(index));
        });

        gui.addPane(borderPane);

        StaticPane navPane = new StaticPane(0, 5, 9, 1);
        navPane.addItem(new GuiItem(arrow(false), event -> {
            int newPage = paginatedPane.getPage() - 1;
            if (newPage < 0)
                newPage = paginatedPane.getPages() - 1;
            paginatedPane.setPage(newPage);
            navPane.addItem(new GuiItem(pageItem(paginatedPane.getPage(), paginatedPane.getPages())), 4, 0);
            gui.update();

        }), 3, 0);
        navPane.addItem(new GuiItem(arrow(true), event -> {
            int newPage = paginatedPane.getPage() + 1;
            if (newPage > paginatedPane.getPages() - 1)
                newPage = 0;
            paginatedPane.setPage(newPage);
            navPane.addItem(new GuiItem(pageItem(paginatedPane.getPage(), paginatedPane.getPages())), 4, 0);
            gui.update();

        }), 5, 0);

        navPane.addItem(new GuiItem(pageItem(paginatedPane.getPage(), paginatedPane.getPages())), 4, 0);

        if (!path.equals(this.plugin.getDataPath().resolve("songs"))) {
            borderPane.removeItem(Slot.fromIndex(45));
            ItemStack backButton = new ItemStack(Material.ARROW);
            backButton.setData(DataComponentTypes.CUSTOM_NAME,
                    cmp.formatMessage("<!i><#99d98c><b>Go Back</b></#99d98c>"));
            navPane.addItem(new GuiItem(backButton, event -> {
                openGUI(path.getParent());
            }), 0, 0);

        }

        gui.addPane(navPane);
        gui.show(this.player);

    }

    private ItemStack pageItem(int page, int maxPage) {
        ItemStack currentPage = new ItemStack(Material.PAPER);
        currentPage.setData(DataComponentTypes.CUSTOM_NAME,
                cmp.formatMessage(
                        "<!i><#99d98c><b>Current Page:</b></#99d98c> <#99e2b4><page><#99d98c>/</#99d98c><max></#99e2b4>",
                        Placeholder.unparsed("page", String.valueOf(page + 1)),
                        Placeholder.unparsed("max", String.valueOf(maxPage))));
        return currentPage;
    }
}
