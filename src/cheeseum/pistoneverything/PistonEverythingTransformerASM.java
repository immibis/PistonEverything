package cheeseum.pistoneverything;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;

import java.util.ListIterator;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import cheeseum.pistoneverything.PistonEverythingObfuscationMapper.MethodData;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.FMLInjectionData;

public class PistonEverythingTransformerASM implements IClassTransformer, Opcodes
{
	private PistonEverythingObfuscationMapper obfMapper;
	
	public PistonEverythingTransformerASM ()
	{
		obfMapper = new PistonEverythingObfuscationMapper(PistonEverythingLoadingPlugin.runtimeDeobfuscationEnabled);
		obfMapper.loadMappings("/deobfuscation_data-" + FMLInjectionData.data()[4] + ".lzma");
	}
	
	private byte[] transformTileEntityPiston(String className, byte[] in)
	{
		ClassNode cNode = new ClassNode();
		ClassReader cr = new ClassReader(in);
		cr.accept(cNode, 0);
		
		// class, field, and method mappings
		String c_NBTTagCompound = obfMapper.getClassMapping("net/minecraft/nbt/NBTTagCompound", "NBTTagCompound");
		String c_World = obfMapper.getClassMapping("net/minecraft/world/World", "World");
		String f_worldObj = obfMapper.getFieldMapping("worldObj", "field_70331_k");
		String f_xCoord = obfMapper.getFieldMapping("xCoord", "field_70329_l");
		String f_yCoord = obfMapper.getFieldMapping("yCoord", "field_70333_m");
		String f_zCoord = obfMapper.getFieldMapping("zCoord", "field_70327_n");
		String f_storedBlockID = obfMapper.getFieldMapping("storedBlockID", "field_70348_a");
		String f_storedMetadata = obfMapper.getFieldMapping("storedMetadata", "field_70346_b");
		MethodData m_removeBlockTileEntity = obfMapper.getMethodMapping("removeBlockTileEntity", "func_72932_q", "(III)V");
		MethodData m_clearPistonTileEntity = obfMapper.getMethodMapping("clearPistonTileEntity", "func_70339_i", "()V");
		MethodData m_updateEntity = obfMapper.getMethodMapping("updateEntity", "func_70316_g", "()V");
		
		cNode.fields.add(new FieldNode(ACC_PRIVATE, "storedTileEntityData", "Lnet/minecraft/nbt/NBTTagCompound;", null, null));
		
		// storeTileEntity
		{
			MethodNode mn = new MethodNode(ACC_PUBLIC, "storeTileEntity", "(Lnet/minecraft/nbt/NBTTagCompound;)V", null, null);
			mn.instructions.add(new VarInsnNode(ALOAD, 0));
			mn.instructions.add(new VarInsnNode(ALOAD, 1));
			mn.instructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/tileentity/TileEntityPiston", "storedTileEntityData", "Lnet/minecraft/nbt/NBTTagCompound;"));
			mn.instructions.add(new InsnNode(RETURN));	
			cNode.methods.add(mn);
		}
		
		for (MethodNode mn : cNode.methods)
		{
			if (mn.name.equals("clearPistonTileEntity") || mn.name.equals("updateEntity"))
			{
				ListIterator<AbstractInsnNode> it = mn.instructions.iterator();
				InsnList newInsns = new InsnList();
				while (it.hasNext())
				{
					AbstractInsnNode insn = it.next();
					if (insn instanceof JumpInsnNode && insn.getOpcode() == IF_ICMPNE)
					{
						// code to restore stored tile entities
						LabelNode l1 = new LabelNode();
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, "net/minecraft/tileentity/TileEntityPiston", "storedTileEntityData", "Lnet/minecraft/nbt/NBTTagCompound;"));
						newInsns.add(new JumpInsnNode(IFNULL, l1));
						
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, "net/minecraft/tileentity/TileEntityPiston", "worldObj", "Lnet/minecraft/world/World;"));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, "net/minecraft/tileentity/TileEntityPiston", "xCoord", "I"));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, "net/minecraft/tileentity/TileEntityPiston", "yCoord", "I"));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, "net/minecraft/tileentity/TileEntityPiston", "zCoord", "I"));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, "net/minecraft/tileentity/TileEntityPiston", "storedBlockID", "I"));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, "net/minecraft/tileentity/TileEntityPiston", "storedMetadata", "I"));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, "net/minecraft/tileentity/TileEntityPiston", "storedTileEntityData", "Lnet/minecraft/nbt/NBTTagCompound;"));
						newInsns.add(new MethodInsnNode(INVOKESTATIC, "cheeseum/pistoneverything/PistonEverything", "restoreStoredPistonBlock", "(Lnet/minecraft/world/World;IIIIILnet/minecraft/nbt/NBTTagCompound;)V"));
						newInsns.add(l1);
					}
					
					newInsns.add(insn);								
				}
				mn.instructions = newInsns;
			}
			
		}
		
		ClassWriter cw = new ClassWriter(COMPUTE_FRAMES);
		cNode.accept(cw);
		return cw.toByteArray();
	}
	
	private byte[] transformBlockPistonBase(String className, byte[] in)
	{
		ClassNode cNode = new ClassNode();
		ClassReader cr = new ClassReader(in);
		cr.accept(cNode, 0);
		
		for (MethodNode mn : cNode.methods)
		{
			if (mn.name.equals("onBlockEventReceived"))
			{
				ListIterator<AbstractInsnNode> it = mn.instructions.iterator();
				InsnList newInsns = new InsnList();
				while (it.hasNext())
				{
					AbstractInsnNode insn = it.next();
					newInsns.add(insn);
					
					// find the sticky piston logic
					if (insn instanceof VarInsnNode && insn.getOpcode() == ISTORE && ((VarInsnNode) insn).var == 4)
					{
						// inject tile entity storage code
						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new VarInsnNode(ILOAD, 8));
						newInsns.add(new VarInsnNode(ILOAD, 9));
						newInsns.add(new VarInsnNode(ILOAD, 10));
						newInsns.add(new MethodInsnNode(INVOKESTATIC, "cheeseum/pistoneverything/PistonEverything", "getBlockTileEntityData", "(Lnet/minecraft/world/World;III)Lnet/minecraft/nbt/NBTTagCompound;"));
						newInsns.add(new VarInsnNode(ASTORE, 14));

						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new VarInsnNode(ILOAD, 8));
						newInsns.add(new VarInsnNode(ILOAD, 9));
						newInsns.add(new VarInsnNode(ILOAD, 10));
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", "removeBlockTileEntity", "(III)V"));						
						
						// jump to after the settileentity call
						while (it.hasNext())
						{
							insn = it.next();
							newInsns.add(insn);
							
							if (insn instanceof MethodInsnNode && ((MethodInsnNode) insn).name.equals("setBlockTileEntity"))
							{
								break;
							}
						}
						
						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new VarInsnNode(ILOAD, 2));
						newInsns.add(new VarInsnNode(ILOAD, 3));
						newInsns.add(new VarInsnNode(ILOAD, 4));
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", "getBlockTileEntity", "(III)Lnet/minecraft/tileentity/TileEntity;"));
						newInsns.add(new TypeInsnNode(CHECKCAST, "net/minecraft/tileentity/TileEntityPiston"));
						newInsns.add(new VarInsnNode(ALOAD, 14));
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/tileentity/TileEntityPiston", "storeTileEntity", "(Lnet/minecraft/nbt/NBTTagCompound;)V"));
					}
				}
				
				mn.instructions = newInsns;
			}
			
			if (mn.name.equals("tryExtend"))
			{
				ListIterator<AbstractInsnNode> it = mn.instructions.iterator();
				InsnList newInsns = new InsnList();
				while (it.hasNext())
				{
					AbstractInsnNode insn = it.next();
					newInsns.add(insn);
					
					// find the extending logic
					if (insn instanceof VarInsnNode && insn.getOpcode() == ISTORE && ((VarInsnNode) insn).var == 18)
					{
						// find the code of the else statement
						AbstractInsnNode target = null;
						AbstractInsnNode node = insn;
						while (node != null)
						{
							if (node instanceof JumpInsnNode && node.getOpcode() == IF_ICMPNE)
							{
								target = ((JumpInsnNode) node).label;
								break;
							}
							node = node.getNext();
						}
						
						// inject tile entity storage code 
						// copy+pasted from above with different local variable indicies
						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new VarInsnNode(ILOAD, 14));
						newInsns.add(new VarInsnNode(ILOAD, 15));
						newInsns.add(new VarInsnNode(ILOAD, 16));
						newInsns.add(new MethodInsnNode(INVOKESTATIC, "cheeseum/pistoneverything/PistonEverything", "getBlockTileEntityData", "(Lnet/minecraft/world/World;III)Lnet/minecraft/nbt/NBTTagCompound;"));
						newInsns.add(new VarInsnNode(ASTORE, 19));

						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new VarInsnNode(ILOAD, 14));
						newInsns.add(new VarInsnNode(ILOAD, 15));
						newInsns.add(new VarInsnNode(ILOAD, 16));
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", "removeBlockTileEntity", "(III)V"));						
						
						// skip to the label
						while (it.hasNext()) 
						{	
							insn = it.next();
							newInsns.add(insn);
							
							if (insn.equals(target))
								break;
						}
						
						// skip to after the setBlockTileEntity call
						while (it.hasNext())
						{
							insn = it.next();
							newInsns.add(insn);
							
							if (insn instanceof MethodInsnNode && ((MethodInsnNode) insn).name.equals("setBlockTileEntity"))
								break;
						}
					
						// inject the storage call
						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new VarInsnNode(ILOAD, 6));
						newInsns.add(new VarInsnNode(ILOAD, 7));
						newInsns.add(new VarInsnNode(ILOAD, 8));
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", "getBlockTileEntity", "(III)Lnet/minecraft/tileentity/TileEntity;"));
						newInsns.add(new TypeInsnNode(CHECKCAST, "net/minecraft/tileentity/TileEntityPiston"));
						newInsns.add(new VarInsnNode(ALOAD, 19));
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/tileentity/TileEntityPiston", "storeTileEntity", "(Lnet/minecraft/nbt/NBTTagCompound;)V"));
					}
					
				}
				mn.instructions = newInsns;
			}
			
			if (mn.name.equals("canPushBlock"))
			{
				ListIterator<AbstractInsnNode> it = mn.instructions.iterator();
				
				// find the tileentitycheck
				AbstractInsnNode node = mn.instructions.getFirst();
				while (node != null) {
					node = node.getNext();
					if (node instanceof MethodInsnNode && node.getOpcode() == INVOKEVIRTUAL)
					{
						if (((MethodInsnNode) node).name.equals("blockHasTileEntity"))
						{
							// FIXME: kind of lame way to do this
							// go back 4 instructions before variable loading
							node = node.getPrevious();
							node = node.getPrevious();
							node = node.getPrevious();
							node = node.getPrevious();
							break;
						}
					}
				}
				
				InsnList newInsns = new InsnList();
				while (it.hasNext())
				{	
					AbstractInsnNode insn = it.next();
					newInsns.add(insn);
					
					if (insn.equals(node))
					{
						// inject return true
						// TODO: inject black/whitelist code
						newInsns.add(new InsnNode(ICONST_1));
						newInsns.add(new InsnNode(IRETURN));
						
						// throw out the rest of the tileentity check
						break;
					}
				}
				mn.instructions = newInsns;
			}
		}
		
		ClassWriter cw = new ClassWriter(COMPUTE_FRAMES);
		cNode.accept(cw);
		return cw.toByteArray();
	}
	
	@Override
	public byte[] transform(String arg0, String arg1, byte[] arg2)
	{
		//String className = remapper.map(arg0).replace('/','.');
		String className = arg1;
		if (className.equals("net.minecraft.tileentity.TileEntityPiston")) 
		{
			FMLLog.info("Patching class %s!", className);
			return transformTileEntityPiston(className, arg2);
		} 
		else if (className.equals("net.minecraft.block.BlockPistonBase"))
		{
			FMLLog.info("Patching class %s!", className);
			return transformBlockPistonBase(className, arg2);
		}
		
		return arg2;
	}
}
