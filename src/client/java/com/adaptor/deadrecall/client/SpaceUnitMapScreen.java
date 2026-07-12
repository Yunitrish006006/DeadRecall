package com.adaptor.deadrecall.client;

import com.adaptor.deadrecall.network.RequestSpaceUnitMapPayload;
import com.adaptor.deadrecall.network.SpaceUnitMapPayload;
import com.adaptor.deadrecall.network.StartSpaceUnitTeleportPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SpaceUnitMapScreen extends Screen {
    public static SpaceUnitMapScreen CURRENT = null;

    private static final int PANEL_WIDTH = 640;
    private static final int PANEL_HEIGHT = 360;
    private static final int PANEL_PADDING = 12;
    private static final int HEADER_HEIGHT = 34;
    private static final int TAB_HEIGHT = 20;
    private static final int FOOTER_HEIGHT = 46;
    private static final int GAP = 10;
    private static final int LIST_WIDTH = 196;
    private static final int ROW_HEIGHT = 30;
    private static final int MIN_MAP_SIZE = 32;
    private static final double MIN_ZOOM = 0.35D;
    private static final double MAX_ZOOM = 5.0D;

    private SpaceUnitMapPayload payload;
    private List<String> dimensions;
    private String activeDimension;
    private UUID selectedUnitId;
    private int listScrollIndex = 0;
    private double zoom = 1.0D;
    private Button teleportButton;
    private Button refreshButton;
    private Button doneButton;

    public SpaceUnitMapScreen(SpaceUnitMapPayload payload) {
        super(Component.translatable("container.deadrecall.space_unit.map"));
        this.payload = payload;
        this.dimensions = collectDimensions(payload);
        this.activeDimension = dimensions.contains(payload.sourceDimension())
                ? payload.sourceDimension()
                : dimensions.isEmpty() ? payload.sourceDimension() : dimensions.get(0);
        this.selectedUnitId = payload.sourceUnitId();
        CURRENT = this;
    }

    @Override
    public void removed() {
        super.removed();
        if (CURRENT == this) {
            CURRENT = null;
        }
    }

    public boolean isFor(String sourceType, UUID sourceUnitId) {
        return this.payload.sourceType().equals(sourceType) && this.payload.sourceUnitId().equals(sourceUnitId);
    }

    public void applyPayload(SpaceUnitMapPayload payload) {
        UUID previousSelection = this.selectedUnitId;
        String previousDimension = this.activeDimension;
        this.payload = payload;
        this.dimensions = collectDimensions(payload);
        this.activeDimension = this.dimensions.contains(previousDimension)
                ? previousDimension
                : this.dimensions.contains(payload.sourceDimension())
                ? payload.sourceDimension()
                : this.dimensions.isEmpty() ? payload.sourceDimension() : this.dimensions.get(0);
        this.selectedUnitId = containsEntry(previousSelection) || payload.sourceUnitId().equals(previousSelection)
                ? previousSelection
                : payload.sourceUnitId();
        this.listScrollIndex = Math.min(this.listScrollIndex, getMaxListScrollIndex());
    }

    @Override
    protected void init() {
        this.teleportButton = Button.builder(Component.translatable("message.deadrecall.space_unit.teleport_start"), button -> requestTeleport())
                .bounds(panelX() + panelWidth() - PANEL_PADDING - 238, panelY() + panelHeight() - 23, 72, 18)
                .build();
        this.addRenderableWidget(this.teleportButton);

        this.refreshButton = Button.builder(Component.translatable("message.deadrecall.space_unit.map_refresh"), button -> requestRefresh())
                .bounds(panelX() + panelWidth() - PANEL_PADDING - 160, panelY() + panelHeight() - 23, 72, 18)
                .build();
        this.addRenderableWidget(this.refreshButton);

        this.doneButton = Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                .bounds(panelX() + panelWidth() - PANEL_PADDING - 82, panelY() + panelHeight() - 23, 70, 18)
                .build();
        this.addRenderableWidget(this.doneButton);
        updateButtonLayout();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        extractor.fill(0, 0, this.width, this.height, 0xA0000000);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        updateButtonLayout();
        int panelX = panelX();
        int panelY = panelY();
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();

        extractor.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xEA16191D);
        extractor.outline(panelX, panelY, panelWidth, panelHeight, 0xFF657383);
        extractor.text(this.font, this.title, panelX + PANEL_PADDING, panelY + 9, 0xFFFFFFFF);
        extractor.text(this.font, sourceSummary(), panelX + PANEL_PADDING + 150, panelY + 9, 0xFFB8C0C8);

        drawDimensionTabs(extractor, mouseX, mouseY);
        drawMap(extractor, mouseX, mouseY);
        drawNodeList(extractor, mouseX, mouseY);
        drawFooter(extractor);

        super.extractRenderState(extractor, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (selectDimensionAt(event.x(), event.y())) {
            return true;
        }

        UUID mapHit = mapEntryAt(event.x(), event.y());
        if (mapHit != null) {
            this.selectedUnitId = mapHit;
            ensureSelectedVisible();
            return true;
        }

        UUID rowHit = listEntryAt(event.x(), event.y());
        if (rowHit != null) {
            this.selectedUnitId = rowHit;
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isInside(mouseX, mouseY, mapX(), mapY(), mapWidth(), mapHeight())) {
            if (verticalAmount > 0) {
                this.zoom = Math.min(MAX_ZOOM, this.zoom * 1.15D);
            } else if (verticalAmount < 0) {
                this.zoom = Math.max(MIN_ZOOM, this.zoom / 1.15D);
            }
            return true;
        }

        if (isInside(mouseX, mouseY, listX(), listY(), listWidth(), listHeight())) {
            if (verticalAmount < 0) {
                this.listScrollIndex = Math.min(getMaxListScrollIndex(), this.listScrollIndex + 1);
                return true;
            }
            if (verticalAmount > 0) {
                this.listScrollIndex = Math.max(0, this.listScrollIndex - 1);
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void requestRefresh() {
        if (ClientPlayNetworking.canSend(RequestSpaceUnitMapPayload.TYPE)) {
            ClientPlayNetworking.send(new RequestSpaceUnitMapPayload(this.payload.sourceType(), this.payload.sourceUnitId()));
        }
    }

    private void requestTeleport() {
        SpaceUnitMapPayload.Entry selected = selectedEntry();
        if (selected == null || !selected.canTeleport()) {
            return;
        }

        if (ClientPlayNetworking.canSend(StartSpaceUnitTeleportPayload.TYPE)) {
            ClientPlayNetworking.send(new StartSpaceUnitTeleportPayload(
                    this.payload.sourceType(),
                    this.payload.sourceUnitId(),
                    selected.id()
            ));
            this.onClose();
        }
    }

    private void drawDimensionTabs(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
        int x = panelX() + PANEL_PADDING;
        int y = panelY() + HEADER_HEIGHT;
        int maxRight = panelX() + panelWidth() - PANEL_PADDING;

        for (String dimension : this.dimensions) {
            int tabWidth = Math.min(132, Math.max(64, this.font.width(shortDimension(dimension)) + 18));
            if (x + tabWidth > maxRight) {
                extractor.text(this.font, Component.translatable("message.deadrecall.space_unit.map_more_dimensions"), x + 4, y + 6, 0xFF9AA3AD);
                return;
            }

            boolean active = dimension.equals(this.activeDimension);
            boolean hovered = isInside(mouseX, mouseY, x, y, tabWidth, 18);
            extractor.fill(x, y, x + tabWidth, y + 18, active ? 0xFF304154 : hovered ? 0xFF27313D : 0xFF20262E);
            extractor.outline(x, y, tabWidth, 18, active ? 0xFF78A6D6 : 0xFF4B5663);
            extractor.text(this.font, trimToWidth(shortDimension(dimension), tabWidth - 10), x + 5, y + 5, active ? 0xFFFFFFFF : 0xFFC8D0D8);
            x += tabWidth + 4;
        }
    }

    private void drawMap(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
        int x = mapX();
        int y = mapY();
        int width = mapWidth();
        int height = mapHeight();
        extractor.fill(x, y, x + width, y + height, 0xFF0E1115);
        extractor.outline(x, y, width, height, 0xFF3F4A56);

        int centerX = x + width / 2;
        int centerY = y + height / 2;
        drawGrid(extractor, x, y, width, height, centerX, centerY);
        extractor.fill(centerX, y + 1, centerX + 1, y + height - 1, 0x804E6C88);
        extractor.fill(x + 1, centerY, x + width - 1, centerY + 1, 0x804E6C88);

        drawSourcePoint(extractor, centerX, centerY);
        for (SpaceUnitMapPayload.Entry entry : entriesForActiveDimension()) {
            if (entry.id().equals(this.payload.sourceUnitId())) {
                continue;
            }
            int pointX = mapPointX(entry);
            int pointY = mapPointY(entry);
            if (!isInside(pointX, pointY, x + 2, y + 2, width - 4, height - 4)) {
                continue;
            }
            boolean selected = entry.id().equals(this.selectedUnitId);
            boolean hovered = Math.abs(mouseX - pointX) <= 5 && Math.abs(mouseY - pointY) <= 5;
            int color = colorForType(entry.type());
            int radius = selected || hovered ? 4 : 3;
            extractor.fill(pointX - radius, pointY - radius, pointX + radius + 1, pointY + radius + 1, color);
            extractor.outline(pointX - radius - 1, pointY - radius - 1, radius * 2 + 3, radius * 2 + 3,
                    selected ? 0xFFFFFFFF : 0xFF1A1A1A);
        }

        SpaceUnitMapPayload.Entry selected = selectedEntry();
        if (selected != null && selected.dimension().equals(this.activeDimension)) {
            extractor.text(this.font, trimToWidth(selected.name(), width - 12), x + 6, y + height - 15, 0xFFE8EDF2);
        } else if (entriesForActiveDimension().isEmpty()) {
            extractor.text(this.font, Component.translatable("message.deadrecall.space_unit.map_dimension_empty"), x + 8, y + 8, 0xFFFFC857);
        }
    }

    private void drawGrid(GuiGraphicsExtractor extractor, int x, int y, int width, int height, int centerX, int centerY) {
        double scale = mapScale();
        int gridBlocks = scale >= 3.0D ? 16 : scale >= 1.5D ? 32 : scale >= 0.75D ? 64 : 128;
        int gridPixels = Math.max(8, (int) Math.round(gridBlocks * scale));

        for (int px = centerX % gridPixels; px < width; px += gridPixels) {
            extractor.fill(x + px, y + 1, x + px + 1, y + height - 1, 0x302B3540);
        }
        for (int py = centerY % gridPixels; py < height; py += gridPixels) {
            extractor.fill(x + 1, y + py, x + width - 1, y + py + 1, 0x302B3540);
        }
    }

    private void drawSourcePoint(GuiGraphicsExtractor extractor, int centerX, int centerY) {
        extractor.fill(centerX - 5, centerY - 1, centerX + 6, centerY + 2, 0xFF7DD3FC);
        extractor.fill(centerX - 1, centerY - 5, centerX + 2, centerY + 6, 0xFF7DD3FC);
        extractor.outline(centerX - 6, centerY - 6, 13, 13, 0xFFFFFFFF);
    }

    private void drawNodeList(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
        int x = listX();
        int y = listY();
        int width = listWidth();
        int height = listHeight();
        extractor.fill(x, y, x + width, y + height, 0x80101010);
        extractor.outline(x, y, width, height, 0xFF3F4A56);
        extractor.text(this.font, Component.translatable("message.deadrecall.space_unit.map_nodes", entriesForActiveDimension().size()),
                x + 8, y + 7, 0xFFFFFFFF);

        List<SpaceUnitMapPayload.Entry> entries = entriesForActiveDimension();
        int rowsVisible = Math.max(1, (height - 28) / ROW_HEIGHT);
        int start = Math.min(this.listScrollIndex, Math.max(0, entries.size() - rowsVisible));
        int rowY = y + 24;
        for (int i = start; i < entries.size() && i < start + rowsVisible; i++) {
            SpaceUnitMapPayload.Entry entry = entries.get(i);
            boolean selected = entry.id().equals(this.selectedUnitId);
            boolean hovered = isInside(mouseX, mouseY, x + 4, rowY, width - 12, ROW_HEIGHT - 4);
            extractor.fill(x + 4, rowY, x + width - 8, rowY + ROW_HEIGHT - 4,
                    selected ? 0xFF2D3F54 : hovered ? 0xC02A2F36 : 0x9020252B);
            extractor.outline(x + 4, rowY, width - 12, ROW_HEIGHT - 4, selected ? 0xFF78A6D6 : 0xFF343D47);
            extractor.fill(x + 10, rowY + 8, x + 18, rowY + 16, colorForType(entry.type()));
            extractor.text(this.font, trimToWidth(entry.name(), width - 40), x + 24, rowY + 5, 0xFFFFFFFF);
            extractor.text(this.font, entrySummary(entry), x + 24, rowY + 17, 0xFFB8C0C8);
            rowY += ROW_HEIGHT;
        }

        if (entries.isEmpty()) {
            extractor.text(this.font, Component.translatable("message.deadrecall.space_unit.map_dimension_empty"), x + 8, y + 28, 0xFFFFC857);
        } else if (entries.size() > rowsVisible) {
            drawScrollBar(extractor, x + width - 5, y + 24, height - 30, entries.size(), rowsVisible, start);
        }
    }

    private void drawFooter(GuiGraphicsExtractor extractor) {
        SpaceUnitMapPayload.Entry selected = selectedEntry();
        int x = panelX() + PANEL_PADDING;
        int y = panelY() + panelHeight() - FOOTER_HEIGHT + 6;
        int width = panelWidth() - PANEL_PADDING * 2 - 254;
        String title = selected == null
                ? Component.translatable("message.deadrecall.space_unit.map_source_footer", this.payload.sourceName()).getString()
                : Component.translatable(
                        "message.deadrecall.space_unit.map_selected_footer",
                        selected.name(),
                        localizedType(selected.type()),
                        selected.dimension(),
                        selected.x(),
                        selected.y(),
                        selected.z()).getString();
        extractor.text(this.font, trimToWidth(title, width), x, y, 0xFFE0E6EC);

        if (selected == null) {
            return;
        }

        extractor.text(this.font, trimToWidth(quoteSummary(selected), width), x, y + 12,
                selected.canTeleport() ? 0xFFB8C0C8 : 0xFFFFC857);
        if (!selected.canTeleport() && selected.blockedReason() != null && !selected.blockedReason().isBlank()) {
            extractor.text(this.font, trimToWidth(Component.translatable(selected.blockedReason()).getString(), width), x, y + 24, 0xFFFFD166);
        }
    }

    private void drawScrollBar(GuiGraphicsExtractor extractor, int x, int y, int height, int totalRows, int visibleRows, int start) {
        int thumbHeight = Math.max(16, height * visibleRows / Math.max(visibleRows, totalRows));
        int thumbTravel = Math.max(1, height - thumbHeight);
        int maxStart = Math.max(1, totalRows - visibleRows);
        int thumbY = y + thumbTravel * start / maxStart;
        extractor.fill(x, y, x + 3, y + height, 0x80333333);
        extractor.fill(x, thumbY, x + 3, thumbY + thumbHeight, 0xFF9A9A9A);
    }

    private boolean selectDimensionAt(double mouseX, double mouseY) {
        int x = panelX() + PANEL_PADDING;
        int y = panelY() + HEADER_HEIGHT;
        int maxRight = panelX() + panelWidth() - PANEL_PADDING;

        for (String dimension : this.dimensions) {
            int tabWidth = Math.min(132, Math.max(64, this.font.width(shortDimension(dimension)) + 18));
            if (x + tabWidth > maxRight) {
                return false;
            }
            if (isInside(mouseX, mouseY, x, y, tabWidth, 18)) {
                this.activeDimension = dimension;
                this.listScrollIndex = 0;
                if (entriesForActiveDimension().stream().noneMatch(entry -> entry.id().equals(this.selectedUnitId))) {
                    this.selectedUnitId = dimension.equals(this.payload.sourceDimension())
                            ? this.payload.sourceUnitId()
                            : entriesForActiveDimension().stream().findFirst().map(SpaceUnitMapPayload.Entry::id).orElse(this.payload.sourceUnitId());
                }
                return true;
            }
            x += tabWidth + 4;
        }
        return false;
    }

    private UUID mapEntryAt(double mouseX, double mouseY) {
        if (!isInside(mouseX, mouseY, mapX(), mapY(), mapWidth(), mapHeight())) {
            return null;
        }

        int centerX = mapX() + mapWidth() / 2;
        int centerY = mapY() + mapHeight() / 2;
        if (Math.abs(mouseX - centerX) <= 6 && Math.abs(mouseY - centerY) <= 6) {
            return this.payload.sourceUnitId();
        }

        SpaceUnitMapPayload.Entry best = null;
        double bestDistance = 36.0D;
        for (SpaceUnitMapPayload.Entry entry : entriesForActiveDimension()) {
            int pointX = mapPointX(entry);
            int pointY = mapPointY(entry);
            double dx = mouseX - pointX;
            double dy = mouseY - pointY;
            double distance = dx * dx + dy * dy;
            if (distance <= bestDistance) {
                bestDistance = distance;
                best = entry;
            }
        }
        return best == null ? null : best.id();
    }

    private UUID listEntryAt(double mouseX, double mouseY) {
        if (!isInside(mouseX, mouseY, listX(), listY(), listWidth(), listHeight())) {
            return null;
        }

        List<SpaceUnitMapPayload.Entry> entries = entriesForActiveDimension();
        int rowsVisible = Math.max(1, (listHeight() - 28) / ROW_HEIGHT);
        int start = Math.min(this.listScrollIndex, Math.max(0, entries.size() - rowsVisible));
        int relativeY = (int) mouseY - (listY() + 24);
        if (relativeY < 0) {
            return null;
        }
        int row = relativeY / ROW_HEIGHT;
        int index = start + row;
        if (index < 0 || index >= entries.size()) {
            return null;
        }
        return entries.get(index).id();
    }

    private void ensureSelectedVisible() {
        List<SpaceUnitMapPayload.Entry> entries = entriesForActiveDimension();
        int selectedIndex = -1;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).id().equals(this.selectedUnitId)) {
                selectedIndex = i;
                break;
            }
        }
        if (selectedIndex < 0) {
            return;
        }

        int rowsVisible = Math.max(1, (listHeight() - 28) / ROW_HEIGHT);
        if (selectedIndex < this.listScrollIndex) {
            this.listScrollIndex = selectedIndex;
        } else if (selectedIndex >= this.listScrollIndex + rowsVisible) {
            this.listScrollIndex = selectedIndex - rowsVisible + 1;
        }
    }

    private int mapPointX(SpaceUnitMapPayload.Entry entry) {
        return mapX() + mapWidth() / 2 + (int) Math.round((entry.x() - this.payload.sourceX()) * mapScale());
    }

    private int mapPointY(SpaceUnitMapPayload.Entry entry) {
        return mapY() + mapHeight() / 2 + (int) Math.round((entry.z() - this.payload.sourceZ()) * mapScale());
    }

    private double mapScale() {
        int maxDistance = 32;
        for (SpaceUnitMapPayload.Entry entry : entriesForActiveDimension()) {
            maxDistance = Math.max(maxDistance, Math.abs(entry.x() - this.payload.sourceX()));
            maxDistance = Math.max(maxDistance, Math.abs(entry.z() - this.payload.sourceZ()));
        }
        double available = Math.max(MIN_MAP_SIZE, Math.min(mapWidth(), mapHeight()) - 28);
        return Math.max(0.02D, available / Math.max(1.0D, maxDistance * 2.0D) * this.zoom);
    }

    private List<SpaceUnitMapPayload.Entry> entriesForActiveDimension() {
        List<SpaceUnitMapPayload.Entry> entries = new ArrayList<>();
        for (SpaceUnitMapPayload.Entry entry : this.payload.entries()) {
            if (entry.dimension().equals(this.activeDimension)) {
                entries.add(entry);
            }
        }
        return entries;
    }

    private SpaceUnitMapPayload.Entry selectedEntry() {
        for (SpaceUnitMapPayload.Entry entry : this.payload.entries()) {
            if (entry.id().equals(this.selectedUnitId)) {
                return entry;
            }
        }
        return null;
    }

    private String sourceSummary() {
        return Component.translatable("message.deadrecall.space_unit.map_source_summary",
                this.payload.sourceName(), this.payload.entries().size()).getString();
    }

    private String entrySummary(SpaceUnitMapPayload.Entry entry) {
        if (entry.dimension().equals(this.payload.sourceDimension())) {
            return Component.translatable(
                    "message.deadrecall.space_unit.map_relative_summary",
                    entry.x() - this.payload.sourceX(),
                    entry.z() - this.payload.sourceZ(),
                    totalFoodCost(entry),
                    seconds(entry.prepareTicks()),
                    Math.round(entry.resonance() * 100.0D)).getString();
        }
        return Component.translatable(
                "message.deadrecall.space_unit.map_absolute_summary",
                entry.x(),
                entry.y(),
                entry.z(),
                totalFoodCost(entry),
                seconds(entry.prepareTicks()),
                Math.round(entry.resonance() * 100.0D)).getString();
    }

    private String quoteSummary(SpaceUnitMapPayload.Entry entry) {
        return Component.translatable(
                "message.deadrecall.space_unit.map_quote_footer",
                distanceLabel(entry),
                entry.saturationCost(),
                entry.hungerCost(),
                entry.foodPointsNeeded(),
                entry.safeFoodPointsAvailable(),
                entry.amethystCost(),
                entry.amethystAvailable(),
                seconds(entry.prepareTicks()),
                Math.round(entry.resonance() * 100.0D),
                entry.maxHorizontalDeviation(),
                entry.damageChancePercent()).getString();
    }

    private String distanceLabel(SpaceUnitMapPayload.Entry entry) {
        return entry.distanceBlocks() < 0
                ? Component.translatable("message.deadrecall.space_unit.map_distance_cross_dimension").getString()
                : Component.translatable("message.deadrecall.space_unit.map_distance_blocks", entry.distanceBlocks()).getString();
    }

    private static int totalFoodCost(SpaceUnitMapPayload.Entry entry) {
        return entry.saturationCost() + entry.hungerCost() + entry.foodPointsNeeded();
    }

    private static int seconds(int ticks) {
        return Math.max(0, (int) Math.ceil(ticks / 20.0D));
    }

    private String localizedType(String type) {
        return Component.translatable("message.deadrecall.space_unit.type." + type).getString();
    }

    private String shortDimension(String dimension) {
        int index = dimension.indexOf(':');
        return index >= 0 && index + 1 < dimension.length() ? dimension.substring(index + 1) : dimension;
    }

    private String trimToWidth(String value, int width) {
        if (this.font.width(value) <= width) {
            return value;
        }
        String ellipsis = "...";
        int ellipsisWidth = this.font.width(ellipsis);
        String trimmed = value;
        while (!trimmed.isEmpty() && this.font.width(trimmed) + ellipsisWidth > width) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + ellipsis;
    }

    private boolean containsEntry(UUID unitId) {
        if (unitId == null) {
            return false;
        }
        for (SpaceUnitMapPayload.Entry entry : this.payload.entries()) {
            if (entry.id().equals(unitId)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> collectDimensions(SpaceUnitMapPayload payload) {
        Set<String> dimensions = new LinkedHashSet<>();
        dimensions.add(payload.sourceDimension());
        for (SpaceUnitMapPayload.Entry entry : payload.entries()) {
            dimensions.add(entry.dimension());
        }
        return new ArrayList<>(dimensions);
    }

    private int getMaxListScrollIndex() {
        int rowsVisible = Math.max(1, (listHeight() - 28) / ROW_HEIGHT);
        return Math.max(0, entriesForActiveDimension().size() - rowsVisible);
    }

    private int colorForType(String type) {
        return switch (type) {
            case "death" -> 0xFFE36A6A;
            case "player" -> 0xFF6AD98F;
            case "temporary" -> 0xFFE2C15A;
            case "system" -> 0xFFC084FC;
            default -> 0xFF76B7E8;
        };
    }

    private void updateButtonLayout() {
        int y = panelY() + panelHeight() - 23;
        if (this.teleportButton != null) {
            this.teleportButton.setX(panelX() + panelWidth() - PANEL_PADDING - 238);
            this.teleportButton.setY(y);
            SpaceUnitMapPayload.Entry selected = selectedEntry();
            this.teleportButton.active = selected != null && selected.canTeleport();
        }
        if (this.refreshButton != null) {
            this.refreshButton.setX(panelX() + panelWidth() - PANEL_PADDING - 160);
            this.refreshButton.setY(y);
        }
        if (this.doneButton != null) {
            this.doneButton.setX(panelX() + panelWidth() - PANEL_PADDING - 82);
            this.doneButton.setY(y);
        }
    }

    private int panelWidth() {
        return Math.min(PANEL_WIDTH, Math.max(300, this.width - 12));
    }

    private int panelHeight() {
        return Math.min(PANEL_HEIGHT, Math.max(250, this.height - 12));
    }

    private int panelX() {
        return (this.width - panelWidth()) / 2;
    }

    private int panelY() {
        return (this.height - panelHeight()) / 2;
    }

    private int mapX() {
        return panelX() + PANEL_PADDING;
    }

    private int mapY() {
        return panelY() + HEADER_HEIGHT + TAB_HEIGHT + 8;
    }

    private int mapWidth() {
        return Math.max(MIN_MAP_SIZE, panelWidth() - PANEL_PADDING * 2 - listWidth() - GAP);
    }

    private int mapHeight() {
        return Math.max(MIN_MAP_SIZE, panelHeight() - HEADER_HEIGHT - TAB_HEIGHT - FOOTER_HEIGHT - 18);
    }

    private int listX() {
        return mapX() + mapWidth() + GAP;
    }

    private int listY() {
        return mapY();
    }

    private int listWidth() {
        return Math.min(LIST_WIDTH, Math.max(142, panelWidth() / 3));
    }

    private int listHeight() {
        return mapHeight();
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
