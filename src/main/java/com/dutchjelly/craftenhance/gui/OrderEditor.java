package com.dutchjelly.craftenhance.gui;

import java.util.List;

import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.model.CraftRecipe;
import com.dutchjelly.craftenhance.Util.GUIButtons;
import com.dutchjelly.craftenhance.Util.RecipeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class OrderEditor implements GUIElement{

	List<CraftRecipe> recipes;
	private GUIContainer container;
	private Inventory[] inventories;
	private int currentPage;
	
	public OrderEditor(GUIContainer container){
		Debug.Send("An instance is being made for an ordereditor");
		this.container = container;
		currentPage = 0;
		recipes = container.getMain().getFileManager().getRecipes();
		generateInventory();
	}
	
	private void generateInventory(){
		CraftRecipe add;
		int getIndex;
		inventories = new Inventory[(int) Math.ceil((double)recipes.size() / 45)];
		Debug.Send("The amount of pages in the new recipes viewer is " + inventories.length + ".");
		for(int i = 0; i < inventories.length; i++){
			inventories[i] = Bukkit.createInventory(null, 6*9, "Recipes Viewer");
			for(int j = 0; j < 45; j++){
				getIndex = i*45 + j;
				if(getIndex >= recipes.size()) continue;
				add = recipes.get(getIndex);
				inventories[i].setItem(j, add.getResult());
			}
			addButtons(inventories[i]);
		}
	}
	
	private void addButtons(Inventory inv){
		inv.setItem(47, GUIButtons.previous);
		inv.setItem(51, GUIButtons.next);
		inv.setItem(49, GUIButtons.save);
	}
	
	@Override
	public Inventory getInventory() {
		return inventories.length == 0 ? null : inventories[currentPage];
	}

	@Override
	public boolean isEventTriggerer(Inventory inv) {
		return inv.equals(getInventory());
	}

	@Override
	public void handleEvent(InventoryClickEvent e) {
		if(!(e.getWhoClicked() instanceof Player)) return;
		
		e.setCancelled(true);
		
		Player player = (Player) e.getWhoClicked();
		
		CraftRecipe recipe;
		if(RecipeUtil.IsNullElement(e.getCurrentItem()))
			return;
		recipe = findResultingRecipe(e.getCurrentItem(), e.getRawSlot());
		if(recipe != null){
			if(e.getClick() == ClickType.LEFT)
				move(recipe, -1);
			else if(e.getClick() == ClickType.RIGHT)
				move(recipe, 1);
			generateInventory();
		}
		if(e.getCurrentItem().equals(GUIButtons.next))
			scroll(1);
		else if(e.getCurrentItem().equals(GUIButtons.previous))
			scroll(-1);
		
		else if(e.getCurrentItem().equals(GUIButtons.save))
			container.getMain().getFileManager().overrideSave();
		container.openGUIElement(this, player);
		
	}
	
	private void move(CraftRecipe recipe, int translation){
		int startIndex = translation > 0 ? 0 : -translation;
		int endIndex = translation > 0 ? recipes.size()-translation : recipes.size();
		for(int i = startIndex; i < endIndex; i++)
			if(recipes.get(i).equals(recipe)){
				switchRecipes(i, i + translation);
				return;
			}
	}
	
	private void switchRecipes(int a, int b){
		CraftRecipe temp = recipes.get(a);
		recipes.set(a, recipes.get(b));
		recipes.set(b, temp);
	}
	
	private CraftRecipe findResultingRecipe(ItemStack result, int clickPos){
		if(clickPos > getInventory().getSize()-9) return null;
		int translatedClickPos = currentPage * (getInventory().getSize()-9) + clickPos;
		if(translatedClickPos >= recipes.size()) return null;
		return recipes.get(translatedClickPos);
	}
	
	
	private void scroll(int amount){
		currentPage = Math.abs((currentPage + amount) % inventories.length);
	}

	@Override
	public Class<?> getInstance() {
		return this.getClass();
	}

}
