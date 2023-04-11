package prog1.kotprog.dontstarve.solution.inventory;

import prog1.kotprog.dontstarve.solution.character.Character;
import prog1.kotprog.dontstarve.solution.exceptions.NotImplementedException;
import prog1.kotprog.dontstarve.solution.inventory.items.*;

public class Inventory implements BaseInventory{
    private AbstractItem[] items;
    private EquippableItem itemInHand;

    public Inventory(){
        this(null);
    }

    public Inventory(Character owner){
        this.items = new AbstractItem[10];
        this.itemInHand = null;
    }

    @Override
    public boolean addItem(AbstractItem item){
        if(item == null) return false;

        if(item.getStackSize() > 1){
            for (int i = 0; i < 10; i++) {
                if(item.getAmount() == 0) return true;
                AbstractItem currentSlot = this.getItem(i);

                if(currentSlot == null) continue;

                int amountAfterAdding = currentSlot.getAmount() + item.getAmount();

                if(currentSlot.getType() == item.getType() && !this.isSlotFull(i)){
                    if(amountAfterAdding <= currentSlot.getStackSize()){
                        this.items[i].setAmount(amountAfterAdding);
                        item.setAmount(0);
                        return true;
                    }else{
                        //System.out.println(item.getAmount() - (currentSlot.getStackSize() - currentSlot.getAmount()));
                        item.setAmount(
                                item.getAmount() - (currentSlot.getStackSize() - currentSlot.getAmount())
                        );

                        this.items[i].setAmount(currentSlot.getStackSize());
                        //System.out.println(this.items[i].getAmount());
                    }
                }
                if(item.getAmount() == 0) return true;
            }


            for (int i = 0; i < 10; i++) {
                if(this.getItem(i) == null){
                    if(item.getAmount() <= item.getStackSize()){
                        //System.out.println(item.getAmount());
                        this.items[i] = ItemSpawner.spawnItem(item.getType(), item.getAmount());
                        item.setAmount(0);
                        return true;
                    }else{
                        //System.out.println(item.getAmount());
                        this.items[i] = ItemSpawner.spawnItem(item.getType(), item.getStackSize());
                        item.setAmount(item.getAmount() - item.getStackSize());
                        //System.out.println(item.getAmount());
                        //System.out.println(this.items[i].getAmount());
                    }
                }
            }
            return false;
        }else{
            for (int i = 0; i < 10; i++) {
                if(this.getItem(i) == null){
                    this.items[i] = ItemSpawner.spawnItem(item.getType(), item.getAmount());
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public AbstractItem dropItem(int index){
        if(this.isIndexBad(index) || this.getItem(index) == null){
            return null;
        }else{
            AbstractItem res = this.items[index];
            this.items[index] = null;
            return res;
        }
    }

    @Override
    public boolean removeItem(ItemType type, int amount){
        if(type == null || amount < 1) return false;
        int availableToDelete = 0;
        for (AbstractItem item : this.items){
            if(item != null && item.getType() == type){
                availableToDelete += item.getAmount();
            }
        }

        if(availableToDelete == 0 || availableToDelete < amount) return false;

        for (int i = 0; i < 10; i++) {
            AbstractItem currentSlot = this.getItem(i);
            if(currentSlot != null && currentSlot.getType() == type){
                if(currentSlot.getAmount() == amount){
                    this.items[i] = null;
                    amount = 0;
                    break;
                }else if(currentSlot.getAmount() > amount){
                    this.items[i].setAmount(currentSlot.getAmount() - amount);
                }else{
                    amount -= currentSlot.getAmount();
                    this.items[i] = null;
                }
            }
        }

        return true;
    }

    @Override
    public boolean swapItems(int index1, int index2){
        if(this.isIndexBad(index1, index2) ||
                this.getItem(index1) == null || this.getItem(index2) == null){
            return false;
        }else{
            AbstractItem tmp = this.getItem(index1);
            this.items[index1] = this.getItem(index2);
            this.items[index2] = tmp;
            return true;
        }
    }

    @Override
    public boolean moveItem(int index, int newIndex){
        if(this.isIndexBad(index, newIndex) ||
                this.getItem(index) == null || this.getItem(newIndex) != null){
            return false;
        }else{
            this.items[newIndex] = this.getItem(index);
            this.items[index] = null;
            return true;
        }
    }

    @Override
    public boolean combineItems(int index1, int index2){
        if(this.isIndexBad(index1, index2) || index1 == index2){
            return false;
        }

        if(index1 > index2){
            int tempIndex = index1;
            index1 = index2;
            index2 = tempIndex;
        }

        AbstractItem item1 = this.getItem(index1);
        AbstractItem item2 = this.getItem(index2);

        if(item1 == null || item2 == null ||
                item1.getType() != item2.getType() ||
                item1.getStackSize() == 1 || item2.getStackSize() == 1){
            return false;
        }else{
            int amountToStack = item1.getAmount() + item2.getAmount();
            if(amountToStack <= item1.getStackSize()){
                this.getItem(index1).setAmount(amountToStack);
                this.items[index2] = null;
            }else{
                //int remainingToStack = item2.getAmount() - (item1.getStackSize() - item1.getAmount());
                // pl stackSize = 10 --> 7 + 8 = 15 --> marad 5 <-- 15 % 10 = 5
                int remainingToStack = amountToStack % item1.getStackSize();
                this.getItem(index1).setAmount(item1.getStackSize());
                this.getItem(index2).setAmount(remainingToStack);
            }
        }

        /*
        if(this.getItem(index1).getAmount() == item1.getAmount() &&
                this.getItem(index2).getAmount() == item2.getAmount()){
            return false;
        }
        */

        return true;
    }

    @Override
    public boolean equipItem(int index){
        if(this.isIndexBad(index)) return false;
        
        if(!(this.getItem(index) instanceof EquippableItem) || this.getItem(index) == null){
            return false;
        }else{
            if(this.equippedItem() == null){
                this.itemInHand = (EquippableItem) this.getItem(index);
                this.items[index] = null;
            }else{
                AbstractItem tmp = this.getItem(index);
                this.items[index] = this.equippedItem();
                this.itemInHand = (EquippableItem) tmp;
            }
            return true;
        }
    }

    @Override
    public EquippableItem unequipItem(){
        if(this.equippedItem() != null){
            for (int i = 0; i < 10; i++) {
                if(this.getItem(i) == null){
                    this.items[i] = this.equippedItem();
                    this.itemInHand = null;
                    return null;
                }
            }
            //this.owner.getCurrentPosition().getNearestWholePosition();
            return this.equippedItem();
        }
        return null;
    }

    @Override
    public ItemType cookItem(int index){
        if(this.isIndexBad(index) || this.getItem(index) == null) return null;

        AbstractItem item = this.getItem(index);

        if(item.getType() == ItemType.RAW_BERRY || item.getType() == ItemType.RAW_CARROT){
            //aktuális slottal való elbánás
            if(item.getAmount() > 1){
                this.getItem(index).setAmount(item.getAmount() - 1);
            }else{
                this.items[index] = null;
            }

            //új item elhelyezése
            switch (item.getType()){
                case RAW_BERRY -> this.addItem(new ItemCookedBerry(1));
                case RAW_CARROT -> this.addItem(new ItemCookedCarrot(1));
                default -> {}
            }

        }else return null;

        return item.getType();
    }

    @Override
    public ItemType eatItem(int index){
        throw new NotImplementedException();
        /*
        if(this.isIndexBad(index) || this.getItem(index) == null) throw new IllegalArgumentException();

        AbstractItem item = this.getItem(index);
        if(item.isEdible()){
            if(item.getAmount() == 1) this.items[index] = null;
            else{
                this.getItem(index).setAmount(item.getAmount() - 1);
            }
        }

        return item.getType();

         */
    }

    @Override
    public int emptySlots(){
        int emptyCount = 0;
        for(AbstractItem tmp : this.items){
            if(tmp == null){
                emptyCount++;
            }
        }

        return emptyCount;
    }

    @Override
    public EquippableItem equippedItem(){
        return this.itemInHand;
    }

    @Override
    public AbstractItem getItem(int index){
        if(index < 0 || index > 9){
            return null;
        }else{
            return items[index];
        }
    }
    
    public boolean isIndexBad(int... indices){
        for(int index : indices){
            if(index < 0 || index > 9){
                return true;
            }
        }
        return false;
    }

    public boolean isSlotFull(int index){
        if(isIndexBad(index)) throw new IllegalArgumentException();

        return this.getItem(index).getAmount() == this.getItem(index).getStackSize();
    }

    public void manualAdd(AbstractItem item, int index){
        if(this.getItem(index) == null){
            this.items[index] = ItemSpawner.spawnItem(item.getType(), item.getAmount());
        }
    }
    public void listItems(){
        for (int i = 0; i < 10; i++) {
            AbstractItem item = this.getItem(i);
            if(item != null){
                System.out.println("SLOT " + i + ": " + item.getType() + " " + item.getAmount());
            }
        }
    }
}
