package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.gui.container.AutoCrafterContainer;
import com.Da_Technomancer.essentials.tileentities.AutoCrafterTileEntity;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.RecipeBookCategories;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.crafting.IShapedRecipe;

import java.util.List;

public class AutoCrafterScreen extends ContainerScreen<AutoCrafterContainer> implements IRecipeShownListener{

	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/auto_crafter.png");
	private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation("textures/gui/recipe_button.png");

	private final RecipeBookGui recipeBook = new RecipeBookGui();
	private boolean widthTooNarrow;
	private float time = 0;

	public AutoCrafterScreen(AutoCrafterContainer cont, PlayerInventory playerInventory, ITextComponent text){
		super(cont, playerInventory, text);
		ySize = 186;
		//Fixes a vanilla UI bug- the field needs to be recalculated after changing ySize
		playerInventoryTitleY = ySize - 94;//MCP note: player inventory text overlay y position
	}

	protected void init() {
		super.init();
		widthTooNarrow = width < 379;
		recipeBook.init(width, height, Minecraft.getInstance(), widthTooNarrow, container);
		guiLeft = recipeBook.updateScreenPosition(widthTooNarrow, width, xSize);
		children.add(recipeBook);
		setFocusedDefault(recipeBook);
		addButton(new ImageButton(guiLeft + 5, height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, (p_214076_1_) -> {
			recipeBook.initSearchBar(widthTooNarrow);
			recipeBook.toggleVisibility();
			guiLeft = recipeBook.updateScreenPosition(widthTooNarrow, width, xSize);
			((ImageButton) p_214076_1_).setPosition(guiLeft + 5, height / 2 - 49);
		}));
	}

	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks){
		time += partialTicks;
		renderBackground(matrix);
		if(recipeBook.isVisible() && widthTooNarrow){
			drawGuiContainerBackgroundLayer(matrix, partialTicks, mouseX, mouseY);//MCP note: render screen
			recipeBook.render(matrix, mouseX, mouseY, partialTicks);
		}else{
			recipeBook.render(matrix, mouseX, mouseY, partialTicks);
			super.render(matrix, mouseX, mouseY, partialTicks);
			recipeBook.func_230477_a_(matrix, guiLeft, guiTop, true, partialTicks);//MCP note: renderGhostRecipe
		}

		renderHoveredTooltip(matrix, mouseX, mouseY);
		recipeBook.func_238924_c_(matrix, guiLeft, guiTop, mouseX, mouseY);//MCP note: renderToolTip
		setListenerDefault(recipeBook);
	}

	@Override
	protected boolean isPointInRegion(int p_195359_1_, int p_195359_2_, int p_195359_3_, int p_195359_4_, double p_195359_5_, double p_195359_7_){
		return (!widthTooNarrow || !recipeBook.isVisible()) && super.isPointInRegion(p_195359_1_, p_195359_2_, p_195359_3_, p_195359_4_, p_195359_5_, p_195359_7_);
	}

	@Override
	public boolean mouseClicked(double x, double y, int p_mouseClicked_5_){
		if(recipeBook.mouseClicked(x, y, p_mouseClicked_5_)){
			return true;
		}else{
			return widthTooNarrow && recipeBook.isVisible() || super.mouseClicked(x, y, p_mouseClicked_5_);
		}
	}

	@Override
	protected boolean hasClickedOutside(double p_195361_1_, double p_195361_3_, int p_195361_5_, int p_195361_6_, int p_195361_7_){
		boolean flag = p_195361_1_ < (double) p_195361_5_ || p_195361_3_ < (double) p_195361_6_ || p_195361_1_ >= (double) (p_195361_5_ + xSize) || p_195361_3_ >= (double) (p_195361_6_ + ySize);
		return recipeBook.func_195604_a(p_195361_1_, p_195361_3_, guiLeft, guiTop, xSize, ySize, p_195361_7_) && flag;
	}

	/**
	 * Called when the mouse is clicked over a slot or outside the gui.
	 */
	@Override
	protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type){
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
		recipeBook.slotClicked(slotIn);
	}

	@Override
	public void recipesUpdated(){
		recipeBook.recipesUpdated();
	}

	@Override
	public void onClose(){
		recipeBook.removed();
		super.onClose();
	}

	@Override
	public RecipeBookGui getRecipeGui(){
		return recipeBook;
	}

	@Override
	public void tick(){
		super.tick();
		recipeBook.tick();
	}

	protected ResourceLocation getBackgroundTexture(){
		return GUI_TEXTURE;
	}

	//MCP note: render screen
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float p_230450_2_, int p_230450_3_, int p_230450_4_){
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bindTexture(getBackgroundTexture());
		//draw background
		blit(matrix, guiLeft, guiTop, 0, 0, xSize, ySize);

		//foreground
		if(container.te == null){
			return;
		}

		ItemStack[] inv = new ItemStack[19];
		//We start at 10 because the previous slots are ingredient storage slots not used for this
		for(int i = 10; i < inv.length; i++){
			inv[i] = container.getSlot(i).getStack();
		}
		IRecipe<CraftingInventory> iRecipe = container.te.findRecipe(AutoCrafterTileEntity.prepareCraftingInv(inv), container);

		if(iRecipe != null){
			RenderSystem.enableRescaleNormal();

			RenderHelper.enableStandardItemLighting();

			//If the recipe was set via recipe book/JEI, render the ingredients manually (if it was set via input slots, the slots will render the items for us)
			if(container.te.recipe != null){
				boolean shaped = iRecipe instanceof IShapedRecipe;
				int width = 3;
				if(shaped){
					width = ((IShapedRecipe<CraftingInventory>) iRecipe).getRecipeWidth();
				}

				List<Ingredient> ingredients = iRecipe.getIngredients();
				for(int i = 0; i < ingredients.size(); i++){
					ItemStack[] matching = ingredients.get(i).getMatchingStacks();
					if(matching.length == 0){
						continue;
					}
					ItemStack s = matching[(int) Math.floor(time / 30F) % matching.length];
					itemRenderer.renderItemIntoGUI(s, 44 + 18 * (i % width) + guiLeft, 15 + 18 * (i / width) + guiTop);
				}
			}

			//Render the output
			ItemStack output = iRecipe.getRecipeOutput();
			itemRenderer.renderItemIntoGUI(output, 106 + guiLeft, 33 + guiTop);
			if(output.getCount() > 1){
				itemRenderer.renderItemOverlayIntoGUI(font, output, guiLeft + 106, guiTop + 33, null);
			}

			RenderSystem.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
		}
	}

	public static List<RecipeBookCategories> getRecipeCategories(){
		//Same categories as vanilla crafting table. Remember to change this if new crafting table categories are ever added
		return Lists.newArrayList(RecipeBookCategories.CRAFTING_SEARCH, RecipeBookCategories.CRAFTING_EQUIPMENT, RecipeBookCategories.CRAFTING_BUILDING_BLOCKS, RecipeBookCategories.CRAFTING_MISC, RecipeBookCategories.CRAFTING_REDSTONE);
	}
}
