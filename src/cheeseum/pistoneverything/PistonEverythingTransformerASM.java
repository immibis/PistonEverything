package cheeseum.pistoneverything;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import cheeseum.pistoneverything.PistonEverythingObfuscationMapper.MethodData;
import cpw.mods.fml.common.FMLLog;

public class PistonEverythingTransformerASM implements IClassTransformer, Opcodes
{
	// XXX: this is initialized out in the loading plugin and it feels hacky
	public static PistonEverythingObfuscationMapper obfMapper;
	
	private String fieldDesc (String c)
	{
		return "L" + c + ";";
	}
	
	private boolean methodEquals (MethodNode mn, MethodData mData)
	{
		return mn.name.equals(mData.name) && mn.desc.equals(mData.desc); 
	}

	private boolean methodEquals (MethodInsnNode mn, MethodData mData)
	{
		return mn.name.equals(mData.name) && mn.desc.equals(mData.desc); 
	}
	
	private byte[] transformTileEntityPiston(String className, byte[] in)
	{
		ClassNode cNode = new ClassNode();
		ClassReader cr = new ClassReader(in);
		cr.accept(cNode, 0);
		
		// class, field, and method mappings from mcp
		String c_NBTTagCompound = obfMapper.getClassMapping("net/minecraft/nbt/NBTTagCompound", "NBTTagCompound");
		String c_World = obfMapper.getClassMapping("net/minecraft/world/World", "World");
		String c_TileEntityPiston = obfMapper.getClassMapping("net/minecraft/tileentity/TileEntityPiston", "TileEntityPiston");
		String f_worldObj = obfMapper.getFieldMapping("worldObj", "field_70331_k");
		String f_xCoord = obfMapper.getFieldMapping("xCoord", "field_70329_l");
		String f_yCoord = obfMapper.getFieldMapping("yCoord", "field_70330_m");
		String f_zCoord = obfMapper.getFieldMapping("zCoord", "field_70327_n");
		String f_storedBlockID = obfMapper.getFieldMapping("storedBlockID", "field_70348_a");
		String f_storedMetadata = obfMapper.getFieldMapping("storedMetadata", "field_70346_b");
		MethodData m_clearPistonTileEntity = obfMapper.getMethodMapping("clearPistonTileEntity", "func_70339_i", "()V");
		MethodData m_updateEntity = obfMapper.getMethodMapping("updateEntity", "func_70316_g", "()V");
		MethodData m_readFromNBT = obfMapper.getMethodMapping("readFromNBT", "func_70307_a", "(Lnet/minecraft/nbt/NBTTagCompound;)V");
		MethodData m_writeToNBT = obfMapper.getMethodMapping("writeToNBT", "func_70310_b", "(Lnet/minecraft/nbt/NBTTagCompound;)V");
		MethodData m_getCompoundTag = obfMapper.getMethodMapping("getCompoundTag", "func_74775_l", "(Ljava/lang/String;)Lnet/minecraft/nbt/NBTTagCompound;");
		MethodData m_setCompoundTag = obfMapper.getMethodMapping("setCompoundTag", "func_74766_a", "(Ljava/lang/String;Lnet/minecraft/nbt/NBTTagCompound;)V");
		MethodData m_hasKey = obfMapper.getMethodMapping("hasKey", "func_74764_b", "(Ljava/lang/String;)Z");
		
		cNode.fields.add(new FieldNode(ACC_PRIVATE, "storedTileEntityData", fieldDesc(c_NBTTagCompound), null, null));
		
		// storeTileEntity
		{
			MethodNode mn = new MethodNode(ACC_PUBLIC, "storeTileEntity", "(" + fieldDesc(c_NBTTagCompound) + ")V", null, null);
			mn.instructions.add(new VarInsnNode(ALOAD, 0));
			mn.instructions.add(new VarInsnNode(ALOAD, 1));
			mn.instructions.add(new FieldInsnNode(PUTFIELD, c_TileEntityPiston, "storedTileEntityData", fieldDesc(c_NBTTagCompound)));
			mn.instructions.add(new InsnNode(RETURN));	
			cNode.methods.add(mn);
		}
		
		for (MethodNode mn : cNode.methods)
		{
			if (methodEquals(mn, m_clearPistonTileEntity) || methodEquals(mn, m_updateEntity))
			{
				FMLLog.finest("patching clearPistonTileEntity/updatEntity");
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
						newInsns.add(new FieldInsnNode(GETFIELD, c_TileEntityPiston, "storedTileEntityData", fieldDesc(c_NBTTagCompound)));
						newInsns.add(new JumpInsnNode(IFNULL, l1));
						
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, c_TileEntityPiston, f_worldObj, fieldDesc(c_World)));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, c_TileEntityPiston, f_xCoord, "I"));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, c_TileEntityPiston, f_yCoord, "I"));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, c_TileEntityPiston, f_zCoord, "I"));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, c_TileEntityPiston, f_storedBlockID, "I"));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, c_TileEntityPiston, f_storedMetadata, "I"));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, c_TileEntityPiston, "storedTileEntityData", fieldDesc(c_NBTTagCompound)));
						newInsns.add(new MethodInsnNode(INVOKESTATIC, "cheeseum/pistoneverything/PistonEverything", "restoreStoredPistonBlock", String.format("(%sIIIII%s)V", fieldDesc(c_World), fieldDesc(c_NBTTagCompound))));
						newInsns.add(l1);
					}
					
					newInsns.add(insn);								
				}
				mn.instructions = newInsns;
			}
			
			if (methodEquals(mn, m_readFromNBT))
			{
				FMLLog.finest("patching readFromNBT");
				ListIterator<AbstractInsnNode> it = mn.instructions.iterator();
				InsnList newInsns = new InsnList();
				while (it.hasNext())
				{
					AbstractInsnNode insn = it.next();
					if (insn.getOpcode() == RETURN)
					{
						LabelNode l1 = new LabelNode();
						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new LdcInsnNode("blockTileEntity"));
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, c_NBTTagCompound, m_hasKey.name, m_hasKey.desc));
						newInsns.add(new JumpInsnNode(IFEQ, l1));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new LdcInsnNode("blockTileEntity"));
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, c_NBTTagCompound, m_getCompoundTag.name, m_getCompoundTag.desc));
						newInsns.add(new FieldInsnNode(PUTFIELD, c_TileEntityPiston, "storedTileEntityData", fieldDesc(c_NBTTagCompound)));
						newInsns.add(l1);
					}
					
					newInsns.add(insn);
				}
				mn.instructions = newInsns;
			}

			if (methodEquals(mn, m_writeToNBT))
			{
				FMLLog.finest("patching writeToNBT");
				ListIterator<AbstractInsnNode> it = mn.instructions.iterator();
				InsnList newInsns = new InsnList();
				while (it.hasNext())
				{
					AbstractInsnNode insn = it.next();
					if (insn.getOpcode() == RETURN)
					{
						LabelNode l1 = new LabelNode();
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, c_TileEntityPiston, "storedTileEntityData", fieldDesc(c_NBTTagCompound)));
						newInsns.add(new JumpInsnNode(IFNULL, l1));
						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new LdcInsnNode("blockTileEntity"));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, c_TileEntityPiston, "storedTileEntityData", fieldDesc(c_NBTTagCompound)));
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, c_NBTTagCompound, m_setCompoundTag.name, m_setCompoundTag.desc));
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
		
		// class, field, and method mappings
		String c_NBTTagCompound = obfMapper.getClassMapping("net/minecraft/nbt/NBTTagCompound", "NBTTagCompound");
		String c_World = obfMapper.getClassMapping("net/minecraft/world/World", "World");
		String c_TileEntityPiston = obfMapper.getClassMapping("net/minecraft/tileentity/TileEntityPiston", "TileEntityPiston");
		MethodData m_onBlockEventReceived = obfMapper.getMethodMapping("onBlockEventReceived", "func_71883_b", "(Lnet/minecraft/world/World;IIIII)Z");
		MethodData m_getBlockTileEntity = obfMapper.getMethodMapping("getBlockTileEntity", "func_72796_p", "(III)Lnet/minecraft/tileentity/TileEntity;");
		MethodData m_setBlockTileEntity = obfMapper.getMethodMapping("setBlockTileEntity", "func_72837_a", "(IIILnet/minecraft/tileentity/TileEntity;)V");
		MethodData m_removeBlockTileEntity = obfMapper.getMethodMapping("removeBlockTileEntity", "func_72932_q", "(III)V");
		MethodData m_blockHasTileEntity = obfMapper.getMethodMapping("blockHasTileEntity", "func_72927_d", "(III)Z");
		MethodData m_tryExtend = obfMapper.getMethodMapping("tryExtend", "func_72115_j", "(Lnet/minecraft/world/World;IIII)Z");
		MethodData m_canPushBlock = obfMapper.getMethodMapping("canPushBlock", "func_72111_a", "(ILnet/minecraft/world/World;IIIZ)Z");
		
		for (MethodNode mn : cNode.methods)
		{
			if (methodEquals(mn, m_onBlockEventReceived))
			{
				FMLLog.finest("patching onBlockEventReceived...");
				ListIterator<AbstractInsnNode> it = mn.instructions.iterator();
				InsnList newInsns = new InsnList();
				while (it.hasNext())
				{
					AbstractInsnNode insn = it.next();
					newInsns.add(insn);
					
					// find the sticky piston logic
					if (insn instanceof VarInsnNode && insn.getOpcode() == ISTORE && ((VarInsnNode) insn).var == 4)
					{
						FMLLog.finest("patching sticky piston logic");
						// inject tile entity storage code
						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new VarInsnNode(ILOAD, 8));
						newInsns.add(new VarInsnNode(ILOAD, 9));
						newInsns.add(new VarInsnNode(ILOAD, 10));
						newInsns.add(new MethodInsnNode(INVOKESTATIC, "cheeseum/pistoneverything/PistonEverything", "getBlockTileEntityData", String.format("(%sIII)%s", fieldDesc(c_World), fieldDesc(c_NBTTagCompound))));
						newInsns.add(new VarInsnNode(ASTORE, 14));

						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new VarInsnNode(ILOAD, 8));
						newInsns.add(new VarInsnNode(ILOAD, 9));
						newInsns.add(new VarInsnNode(ILOAD, 10));
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, c_World, m_removeBlockTileEntity.name, m_removeBlockTileEntity.desc));						
						
						// jump to after the settileentity call
						while (it.hasNext())
						{
							insn = it.next();
							newInsns.add(insn);
							
							if (insn instanceof MethodInsnNode && methodEquals((MethodInsnNode) insn, m_setBlockTileEntity))
							{
								FMLLog.finest("found setBlockTileEntity");
								break;
							}
						}
						
						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new VarInsnNode(ILOAD, 2));
						newInsns.add(new VarInsnNode(ILOAD, 3));
						newInsns.add(new VarInsnNode(ILOAD, 4));
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, c_World, m_getBlockTileEntity.name, m_getBlockTileEntity.desc));
						newInsns.add(new TypeInsnNode(CHECKCAST, c_TileEntityPiston));
						newInsns.add(new VarInsnNode(ALOAD, 14));
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, c_TileEntityPiston, "storeTileEntity", String.format("(%s)V", fieldDesc(c_NBTTagCompound))));
					}
				}
				
				mn.instructions = newInsns;
			}
			
			if (methodEquals(mn, m_tryExtend))
			{
				FMLLog.finest("patching tryExtend...");
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
						newInsns.add(new MethodInsnNode(INVOKESTATIC, "cheeseum/pistoneverything/PistonEverything", "getBlockTileEntityData", String.format("(%sIII)%s", fieldDesc(c_World), fieldDesc(c_NBTTagCompound))));
						newInsns.add(new VarInsnNode(ASTORE, 19));

						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new VarInsnNode(ILOAD, 14));
						newInsns.add(new VarInsnNode(ILOAD, 15));
						newInsns.add(new VarInsnNode(ILOAD, 16));
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, c_World, m_removeBlockTileEntity.name, m_removeBlockTileEntity.desc));						
						
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
							
							if (insn instanceof MethodInsnNode && methodEquals(((MethodInsnNode) insn), m_setBlockTileEntity))
							{
								FMLLog.finest("found setBlockTileEntity");
								break;
							}
						}
					
						// inject the storage call
						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new VarInsnNode(ILOAD, 6));
						newInsns.add(new VarInsnNode(ILOAD, 7));
						newInsns.add(new VarInsnNode(ILOAD, 8));
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, c_World, m_getBlockTileEntity.name, m_getBlockTileEntity.desc));
						newInsns.add(new TypeInsnNode(CHECKCAST, c_TileEntityPiston));
						newInsns.add(new VarInsnNode(ALOAD, 19));
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, c_TileEntityPiston, "storeTileEntity", String.format("(%s)V", fieldDesc(c_NBTTagCompound))));
					}
					
				}
				mn.instructions = newInsns;
			}
			
			if (methodEquals(mn, m_canPushBlock))
			{
				FMLLog.finest("patching canPushBlock...");
				
				ListIterator<AbstractInsnNode> it = mn.instructions.iterator();
				
				// find the tileentitycheck
				AbstractInsnNode node = mn.instructions.getFirst();
				while (node != null) {
					node = node.getNext();
					if (node instanceof MethodInsnNode && node.getOpcode() == INVOKEVIRTUAL)
					{
						if (methodEquals(((MethodInsnNode) node), m_blockHasTileEntity))
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
						FMLLog.finest("injecting block whitelist check");
						// inject black/whitelist code
						newInsns.add(new VarInsnNode(ILOAD, 0));
						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new VarInsnNode(ILOAD, 2));
						newInsns.add(new VarInsnNode(ILOAD, 3));
						newInsns.add(new VarInsnNode(ILOAD, 4));
						newInsns.add(new MethodInsnNode(INVOKESTATIC, "cheeseum/pistoneverything/PistonEverything", "isBlockWhitelisted", String.format("(I%sIII)Z", fieldDesc(c_World))));
						newInsns.add(new InsnNode(IRETURN));
						
						// throw out the rest of the tileentity check
						FMLLog.finest("removing TileEntity check");
						break;
					}
				}
				mn.instructions = newInsns;
			}
		}
		
		ClassWriter cw = new ClassWriter(COMPUTE_FRAMES);
		cNode.accept(cw);
		try {
            DataOutputStream dout = new DataOutputStream(new FileOutputStream(new File("/tmp", className + ".class")));
            dout.write(cw.toByteArray());
            dout.flush();
            dout.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
