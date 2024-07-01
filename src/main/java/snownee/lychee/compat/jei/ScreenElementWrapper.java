package snownee.lychee.compat.jei;

import me.shedaniel.math.Rectangle;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.client.gui.ScreenElement;

public class ScreenElementWrapper implements IDrawable {

	public final Rectangle bounds = new Rectangle(16, 16);
	private final ScreenElement element;

	public ScreenElementWrapper(AllGuiTextures element) {
		this.element = element;
		bounds.width = element.width;
		bounds.height = element.height;
	}

	public ScreenElementWrapper(RenderElement element) {
		this.element = element;
		bounds.width = element.getWidth();
		bounds.height = element.getHeight();
	}

	@Override
	public int getWidth() {
		return bounds.width;
	}

	@Override
	public int getHeight() {
		return bounds.height;
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		element.render(guiGraphics, bounds.x, bounds.y);
	}
}