package com.cna.nuclearbomb;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class CheckBomb implements Listener {
    private Nuclearbomb thisplugin;
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        if(event.getInventory().getType()!= InventoryType.CHEST)return;
        //判断是否为箱子
        Inventory BombInventory=event.getInventory();
        ItemStack[] BombStack=BombInventory.getContents();
        NuctrlStruct InitBomb=null;
        int tntamount=0;
        int blazerodamount=0;
        boolean have_MissileDevice=false;
        for(ItemStack CheckNuctrl: BombStack){
            if(CheckNuctrl==null)continue;
            if(CheckNuctrl.getType()==Material.PAPER&&CheckNuctrl.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(Nuclearbomb.plugin,"nuctrlpaper_ctrltext"))){
                Bukkit.getServer().getLogger().info(event.getInventory()+"在玩家"+event.getPlayer()+"开箱时发现导弹控制命令...");
                String nuctrlpaper_ctrltext=CheckNuctrl.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Nuclearbomb.plugin,"nuctrlpaper_ctrltext"), PersistentDataType.STRING);
                InitBomb=checkNuctrl(nuctrlpaper_ctrltext);
            } else if(CheckNuctrl.hashCode()==Nuclearbomb.MissileDevice.hashCode()){
                Bukkit.getServer().getLogger().info(event.getInventory()+"在玩家"+event.getPlayer()+"开箱时中发现导弹...");
                have_MissileDevice=true;
                //确保拥有导弹发射器
            } else if(CheckNuctrl.getType()==Material.TNT){
                tntamount+=CheckNuctrl.getAmount();
            } else if(CheckNuctrl.getType()==Material.BLAZE_ROD){
                blazerodamount+=CheckNuctrl.getAmount();
            }
        }
        if(checkInitBomb(InitBomb)&&have_MissileDevice==true){//发现了导弹识别字条和导弹控制中枢
            final NuctrlStruct Final_InitBomb=InitBomb;
            Player player=(Player)event.getPlayer();
            //player.sendMessage(InitBomb.targetX+" "+InitBomb.targetY+" "+InitBomb.targetZ+" "+InitBomb.FlyHeight+" "+InitBomb.model+" "+tntamount);
            Location BombLocation=BombInventory.getLocation();
            final float Nuc_Power=tntamount;
            int finalBlazerodamount = blazerodamount;
            player.sendMessage(BombLocation+" 您发射了一颗导弹...");
            new BukkitRunnable(){//让导弹移动
                int final_Blazerodamount = finalBlazerodamount;
                Location BombLocation_intime=BombLocation;//第一次
                int speed=1;
                int run_state=0;
                int test_csh=1;
                //0:升空 1:移动 2:下降 3:爆炸 4:ERROR
                @Override
                public void run(){
                    //Bukkit.getServer().getLogger().info("定时任务循环次数："+test_csh);
                    Block runtime_before=BombLocation_intime.getBlock();
                    runtime_before.setType(Material.AIR);//移除原箱

                    //Bukkit.getServer().getLogger().info("对核弹下一步位置进行计算");
                    BombLocation_intime=CelebrateAfter(BombLocation_intime);//计算箱子的下一步位置
                    if(BombLocation_intime==null){
                        //核弹应当已爆炸
                        this.cancel();
                        return;
                    }
                    if(run_state>=4||run_state<0){
                        player.sendMessage(BombLocation_intime+" 导弹在运行中遭遇了错误！");
                        //Bukkit.getServer().getLogger().info("状态异常");
                        if(Final_InitBomb.model==666) {
                            BombLocation_intime.getBlock().getWorld().createExplosion(BombLocation_intime, Nuc_Power*0.8f);
                            //Bukkit.getServer().getLogger().info("触发自毁");
                            BombLocation_intime.getBlock().setType(Material.AIR);
                            BombLocation_intime.add(1,0,0).getBlock().setType(Material.AIR);
                        }
                        this.cancel();
                        return;
                    }//检查导弹运行是否遇到错误
                    if(final_Blazerodamount!=0&&run_state==2){
                        //发射集束炸弹
                        //Bukkit.getServer().getLogger().info("照道理来说这个tick应该发射一个集束炸弹");

                        TNTPrimed cluster_bomb=BombLocation_intime.getWorld().spawn(BombLocation_intime,TNTPrimed.class);
                        cluster_bomb.setVelocity(xz_vector());
                        cluster_bomb.setYield(4.0f);
                        cluster_bomb.setGravity(false);
                        cluster_bomb.setVisualFire(true);
                        cluster_bomb.getPersistentDataContainer().set(new NamespacedKey(Nuclearbomb.plugin,"cluster_bomb"),PersistentDataType.BOOLEAN,true);

                        final_Blazerodamount--;
                    }
                    //设置导弹（箱子）新位置的方块
                    Block runtime_after=BombLocation_intime.getBlock();//导弹的新位置
                    //runtime_after.setType(Material.TNT);
                    //TNT tnt=(TNT)runtime_after.getState();

                    runtime_after.setType(Material.CHEST);
                    Chest bomb_after=(Chest)runtime_after.getState();
                    bomb_after.getInventory().setContents(BombStack);//设置新箱子为导弹原本布局

                    test_csh++;
                }
                private Location CelebrateAfter(Location Before){
                    //计算下一步
                    //int mx=0,my=0,mz=0;//应当被弃用的
                    Location AimLocation=Before.clone(),NewLocation=Before.clone();
                    switch(run_state){
                        case 0->{
                            //Bukkit.getServer().getLogger().info("计算位置：升空");
                            //第一种状态：升空
                            int target_dist=Final_InitBomb.FlyHeight-Before.getBlockY();
                            if(target_dist==0){
                                //Bukkit.getServer().getLogger().info("位置确定，进入下一阶段");
                                run_state=1;
                                break;
                            }
                            //没有完成升空（距离巡航高度还有距离）
                            //Bukkit.getServer().getLogger().info("目前距离升空距距离："+target_dist);
                            AimLocation.add(0,target_dist,0);//确定第一阶段目标位置
                            NewLocation=CelebrateNextStep(Before,AimLocation);//第一步，模拟速度为1的情况
                            break;
                        }case 1->{
                            //Bukkit.getServer().getLogger().info("计算位置：巡航");
                            int x_dist=Final_InitBomb.targetX-Before.getBlockX(),z_dist=Final_InitBomb.targetZ-Before.getBlockZ();
                            if(x_dist==0&&z_dist==0){
                                //Bukkit.getServer().getLogger().info("位置确定，进入下一阶段");
                                run_state=2;
                                break;
                            }//已经到位
                            AimLocation.add(x_dist,0,z_dist);
                            NewLocation=CelebrateNextStep(Before,AimLocation);
                            break;
                        }case 2->{
                            //Bukkit.getServer().getLogger().info("计算位置：下降");
                            int target_dist=Final_InitBomb.targetY-Before.getBlockY();
                            if(target_dist==0){
                                //Bukkit.getServer().getLogger().info("位置确定，进入下一阶段");
                                run_state=3;
                                break;
                            }

                            AimLocation.add(0,target_dist,0);
                            NewLocation=CelebrateNextStep(Before,AimLocation);
                            break;
                        }case 3->{//核弹本体爆炸
                            //Bukkit.getServer().getLogger().info("计算位置：引爆");
                            Before.getBlock().getWorld().createExplosion(Before,Nuc_Power,true);
                            Before.getBlock().setType(Material.AIR);

                            return null;
                        }
                    }
                    //Location After=Before.add(mx,my,mz);
                    //Bukkit.getServer().getLogger().info("计算位置：偏移坐标系："+After);
                    if(NewLocation.getBlock().getType()!=Material.AIR&&NewLocation.getBlock().getType()!=Material.WATER){
                        //Bukkit.getServer().getLogger().info("计算位置：发现阻碍，更改状态");
                        run_state=4;
                        return Before;
                    }//判断是否受到阻碍
                    //Bukkit.getServer().getLogger().info("计算位置：返回坐标系："+After);
                    return NewLocation;
                }//计算导弹下一步位置
                public Location CelebrateNextStep(Location Before,Location Aim){
                    int[] dis=new int[]{Aim.getBlockX()-Before.getBlockX(),Aim.getBlockY()-Before.getBlockY(),Aim.getBlockZ()-Before.getBlockZ()};
                    Location ret;
                    int max_dist=0;
                    int max_dist_size=-1;
                    for(int i=0;i<3;i++){
                        for(int j=0;j<3;j++){
                            if(Math.abs(dis[i])>Math.abs(dis[j])&&Math.abs(dis[i])>=max_dist){
                                max_dist=Math.abs(dis[i]);
                                max_dist_size=i;
                            }
                        }
                    }
                    if(max_dist_size==0){
                        //x++
                        if(dis[0]>0) ret=Before.add(1,0,0);
                        else ret=Before.add(-1,0,0);
                    }else if(max_dist_size==1){
                        //y++
                        if(dis[1]>0) ret=Before.add(0,1,0);
                        else ret=Before.add(0,-1,0);
                    }else if(max_dist_size==2){
                        //z++
                        if(dis[2]>0) ret=Before.add(0,0,1);
                        else ret=Before.add(0,0,-1);
                    }else {
                        ret=Before;
                    }
                    return ret;
                }//计算和目标地点的下一步位置
                public Vector xz_vector(){
                    double ret=0;
                    double key_x=1,key_z=1;
                    double a=Math.random(),b=Math.random(),c=Math.random(),d=Math.random();
                    key_x=key_x*c*0.5;
                    key_z=key_z*d*0.5;
                    if(a>0.5){
                        key_x*=1;
                    }else{
                        key_x*=-1;
                    }
                    if(b>0.5){
                        key_z*=1;
                    }else{
                        key_z*=-1;
                    }
                    return new Vector(key_x,-1,key_z);
                }
            }.runTaskTimer(Nuclearbomb.plugin,0L,1L);
        }else{
            return;
        }
    }
    public NuctrlStruct checkNuctrl(String ctrltext){
        NuctrlStruct ns=new NuctrlStruct();
        String[] arg=ctrltext.split(",");
        if(arg.length!=5)return ns;
        try {
            ns.targetX=Integer.parseInt(arg[0]);
            ns.targetY=Integer.parseInt(arg[1]);
            ns.targetZ=Integer.parseInt(arg[2]);
            ns.FlyHeight=Integer.parseInt(arg[3]);
            ns.model=Integer.parseInt(arg[4]);
            Bukkit.getServer().getLogger().info("检查导弹配置： "+ns.targetX+" "+ns.targetY+" "+ns.targetZ+" "+ns.FlyHeight+" "+ns.model+" ;");
        } catch (NumberFormatException e) {
            Bukkit.getServer().getLogger().info("检查导弹配置：参数错误!");
            e.printStackTrace();
        }
        return ns;
    }
    public boolean checkInitBomb(NuctrlStruct ns){
        if(ns==null)return false;
        if(ns.targetY>319||ns.FlyHeight>319)return false;
        return true;
    }
}
