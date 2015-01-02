package com.cheeseum.pistoneverything;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;

import java.util.ListIterator;

import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.Level;
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

import com.cheeseum.pistoneverything.PistonEverythingObfuscationMapper.MethodData;

public class PistonEverythingTransformerASM implements IClassTransformer, Opcodes
{
	// XXX: this is initialized out in the loading plugin and it feels hacky
	public static PistonEverythingObfuscationMapper obfMapper;
	
	private static String c_PistonEverything;
	private static String c_NBTTagCompound;
	private static String c_World;
	private static String c_TileEntity;
	private static String c_TileEntityPiston;
    private static String c_Block;
    private static String c_RenderBlocks;
	
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

	public static void initClassMappings()
	{
		c_PistonEverything = "com/cheeseum/pistoneverything/PistonEverything";
		c_NBTTagCompound = "net/minecraft/nbt/NBTTagCompound";
		c_World = "net/minecraft/world/World";
		c_TileEntity = "net/minecraft/tileentity/TileEntity";
		c_TileEntityPiston = "net/minecraft/tileentity/TileEntityPiston";
		c_Block = "net/minecraft/block/Block";
		c_RenderBlocks = "net/minecraft/client/renderer/RenderBlocks";
	}
	
	private byte[] transformTileEntityPiston(String className, byte[] in)
	{
		ClassNode cNode = new ClassNode();
		ClassReader cr = new ClassReader(in);
		cr.accept(cNode, 0);
		
		String f_worldObj = obfMapper.getFieldMapping("worldObj", "field_145850_b");
		String f_xCoord = obfMapper.getFieldMapping("xCoord", "field_145851_c");
		String f_yCoord = obfMapper.getFieldMapping("yCoord", "field_145848_d");
		String f_zCoord = obfMapper.getFieldMapping("zCoord", "field_145849_e");
		String f_storedBlock = obfMapper.getFieldMapping("storedBlock", "field_145869_a");
		String f_storedMetadata = obfMapper.getFieldMapping("storedMetadata", "field_145876_i");
		MethodData m_clearPistonTileEntity = obfMapper.getMethodMapping("clearPistonTileEntity", "func_145866_f", "()V");
		MethodData m_updateEntity = obfMapper.getMethodMapping("updateEntity", "func_145845_h", "()V");
		MethodData m_readFromNBT = obfMapper.getMethodMapping("readFromNBT", "func_145839_a", "(Lnet/minecraft/nbt/NBTTagCompound;)V");
		MethodData m_writeToNBT = obfMapper.getMethodMapping("writeToNBT", "func_145841_b", "(Lnet/minecraft/nbt/NBTTagCompound;)V");
		MethodData m_getCompoundTag = obfMapper.getMethodMapping("getCompoundTag", "func_74775_l", "(Ljava/lang/String;)Lnet/minecraft/nbt/NBTTagCompound;");
		MethodData m_setCompoundTag = obfMapper.getMethodMapping("setCompoundTag", "func_74766_a", "(Ljava/lang/String;Lnet/minecraft/nbt/NBTTagCompound;)V");
		MethodData m_setTag = obfMapper.getMethodMapping("setTag", "func_74782_a", "(Ljava/lang/String;Lnet/minecraft/nbt/NBTBase;)V");
		MethodData m_hasKey = obfMapper.getMethodMapping("hasKey", "func_74764_b", "(Ljava/lang/String;)Z");
		
		cNode.fields.add(new FieldNode(ACC_PUBLIC, "storedTileEntityData", fieldDesc(c_NBTTagCompound), null, null));
		cNode.fields.add(new FieldNode(ACC_PUBLIC, "cachedTileEntity", fieldDesc(c_TileEntity), null, null)); // cached te for rendering
		
		for (MethodNode mn : cNode.methods)
		{
			if (methodEquals(mn, m_clearPistonTileEntity) || methodEquals(mn, m_updateEntity))
			{
				PistonEverything.logger.log(Level.TRACE, "patching clearPistonTileEntity/updatEntity");
				ListIterator<AbstractInsnNode> it = mn.instructions.iterator();
				InsnList newInsns = new InsnList();
				while (it.hasNext())
				{
					AbstractInsnNode insn = it.next();
					newInsns.add(insn);
					
					if (insn instanceof JumpInsnNode && insn.getOpcode() == IF_ACMPNE)
					{
                        PistonEverything.logger.log(Level.TRACE, "injecting restoreStoredPistonBlock");

						// code to restore stored tile entities
						LabelNode l1 = new LabelNode();
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, c_TileEntityPiston, "storedTileEntityData", fieldDesc(c_NBTTagCompound)));
						newInsns.add(new JumpInsnNode(IFNULL, l1)); //if not null then continue
						
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, c_TileEntityPiston, f_worldObj, fieldDesc(c_World)));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, c_TileEntityPiston, f_xCoord, "I"));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, c_TileEntityPiston, f_yCoord, "I"));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, c_TileEntityPiston, f_zCoord, "I"));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, c_TileEntityPiston, f_storedBlock, fieldDesc(c_Block)));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new FieldInsnNode(GETFIELD, c_TileEntityPiston, f_storedMetadata, "I"));
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new MethodInsnNode(INVOKESTATIC, c_PistonEverything, "restoreStoredPistonBlock", String.format("(%sIII%sI%s)V", fieldDesc(c_World), fieldDesc(c_Block), fieldDesc(c_TileEntityPiston))));
						newInsns.add(new JumpInsnNode(GOTO, ((JumpInsnNode)insn).label)); // else
						newInsns.add(l1);
					}
				}
				mn.instructions = newInsns;
			}
			
			if (methodEquals(mn, m_readFromNBT))
			{
				PistonEverything.logger.log(Level.TRACE, "patching readFromNBT");
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
				PistonEverything.logger.log(Level.TRACE, "patching writeToNBT");
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
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, c_NBTTagCompound, m_setTag.name, m_setTag.desc));
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
		MethodData m_onBlockEventReceived = obfMapper.getMethodMapping("onBlockEventReceived", "func_149696_a", "(Lnet/minecraft/world/World;IIIII)Z");
		MethodData m_getTileEntity = obfMapper.getMethodMapping("getTileEntity", "func_147438_o", "(III)Lnet/minecraft/tileentity/TileEntity;");
		MethodData m_setTileEntity = obfMapper.getMethodMapping("setTileEntity", "func_147455_a", "(IIILnet/minecraft/tileentity/TileEntity;)V");
		MethodData m_removeTileEntity = obfMapper.getMethodMapping("removeTileEntity", "func_147475_p", "(III)V");
		MethodData m_getBlock = obfMapper.getMethodMapping("getBlock", "func_147439_a", "(III)Lnet/minecraft/block/Block;");
		MethodData m_blockHasTileEntity = obfMapper.getMethodMapping("blockHasTileEntity", "func_72927_d", "(III)Z");
		MethodData m_tryExtend = obfMapper.getMethodMapping("tryExtend", "func_150079_i", "(Lnet/minecraft/world/World;IIII)Z");
		MethodData m_canPushBlock = obfMapper.getMethodMapping("canPushBlock", "func_150080_a", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;IIIZ)Z");

		for (MethodNode mn : cNode.methods)
		{
			if (methodEquals(mn, m_onBlockEventReceived))
			{
				PistonEverything.logger.log(Level.TRACE, "patching onBlockEventReceived...");
				ListIterator<AbstractInsnNode> it = mn.instructions.iterator();
				InsnList newInsns = new InsnList();
				while (it.hasNext())
				{
					AbstractInsnNode insn = it.next();
					newInsns.add(insn);
					
					// find the sticky piston logic
					if (insn instanceof VarInsnNode && insn.getOpcode() == ISTORE && ((VarInsnNode) insn).var == 4)
					{
						PistonEverything.logger.log(Level.TRACE, "patching sticky piston logic");
						// inject tile entity storage code
						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new VarInsnNode(ILOAD, 8));
						newInsns.add(new VarInsnNode(ILOAD, 9));
						newInsns.add(new VarInsnNode(ILOAD, 10));
						newInsns.add(new MethodInsnNode(INVOKESTATIC, c_PistonEverything, "getBlockTileEntityData", String.format("(%sIII)%s", fieldDesc(c_World), fieldDesc(c_NBTTagCompound))));
						newInsns.add(new VarInsnNode(ASTORE, 14));

						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new VarInsnNode(ILOAD, 8));
						newInsns.add(new VarInsnNode(ILOAD, 9));
						newInsns.add(new VarInsnNode(ILOAD, 10));
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, c_World, m_removeTileEntity.name, m_removeTileEntity.desc));						
						
						// jump to after the settileentity call
						while (it.hasNext())
						{
							insn = it.next();
							newInsns.add(insn);
							
							if (insn instanceof MethodInsnNode && methodEquals((MethodInsnNode) insn, m_setTileEntity))
							{
								PistonEverything.logger.log(Level.TRACE, "found setTileEntity");
								break;
							}
						}
						
						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new VarInsnNode(ILOAD, 2));
						newInsns.add(new VarInsnNode(ILOAD, 3));
						newInsns.add(new VarInsnNode(ILOAD, 4));
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, c_World, m_getTileEntity.name, m_getTileEntity.desc));
						newInsns.add(new TypeInsnNode(CHECKCAST, c_TileEntityPiston));
						newInsns.add(new VarInsnNode(ALOAD, 14));
						newInsns.add(new MethodInsnNode(INVOKESTATIC, c_PistonEverything, "storeTileEntity", String.format("(%s%s)V", fieldDesc(c_TileEntityPiston), fieldDesc(c_NBTTagCompound))));
					}
				}
				
				mn.instructions = newInsns;
			}
			
			if (methodEquals(mn, m_tryExtend))
			{
				PistonEverything.logger.log(Level.TRACE, "patching tryExtend...");
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
							if (node instanceof JumpInsnNode && node.getOpcode() == IF_ACMPNE)
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
						newInsns.add(new MethodInsnNode(INVOKESTATIC, c_PistonEverything, "getBlockTileEntityData", String.format("(%sIII)%s", fieldDesc(c_World), fieldDesc(c_NBTTagCompound))));
						newInsns.add(new VarInsnNode(ASTORE, 19));

						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new VarInsnNode(ILOAD, 14));
						newInsns.add(new VarInsnNode(ILOAD, 15));
						newInsns.add(new VarInsnNode(ILOAD, 16));
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, c_World, m_removeTileEntity.name, m_removeTileEntity.desc));						
						
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
							
							if (insn instanceof MethodInsnNode && methodEquals(((MethodInsnNode) insn), m_setTileEntity))
							{
								PistonEverything.logger.log(Level.TRACE, "found setTileEntity");
								break;
							}
						}
					
						// inject the storage call
						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new VarInsnNode(ILOAD, 6));
						newInsns.add(new VarInsnNode(ILOAD, 7));
						newInsns.add(new VarInsnNode(ILOAD, 8));
						newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, c_World, m_getTileEntity.name, m_getTileEntity.desc));
						newInsns.add(new TypeInsnNode(CHECKCAST, c_TileEntityPiston));
						newInsns.add(new VarInsnNode(ALOAD, 19));
						newInsns.add(new MethodInsnNode(INVOKESTATIC, c_PistonEverything, "storeTileEntity", String.format("(%s%s)V", fieldDesc(c_TileEntityPiston), fieldDesc(c_NBTTagCompound))));
					}
					
				}
				mn.instructions = newInsns;
			}
			
			if (methodEquals(mn, m_canPushBlock))
			{
				PistonEverything.logger.log(Level.TRACE, "patching canPushBlock...");
				
				ListIterator<AbstractInsnNode> it = mn.instructions.iterator();
				
				// find the tileentitycheck
				AbstractInsnNode node = mn.instructions.getFirst();
				while (node != null) {
					node = node.getNext();
					if (node instanceof MethodInsnNode && node.getOpcode() == INVOKEVIRTUAL)
					{
						if (methodEquals(((MethodInsnNode) node), m_getBlock))
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
						PistonEverything.logger.log(Level.TRACE, "injecting block whitelist check");
						// inject black/whitelist code
						newInsns.add(new VarInsnNode(ALOAD, 0));
						newInsns.add(new VarInsnNode(ALOAD, 1));
						newInsns.add(new VarInsnNode(ILOAD, 2));
						newInsns.add(new VarInsnNode(ILOAD, 3));
						newInsns.add(new VarInsnNode(ILOAD, 4));
						newInsns.add(new MethodInsnNode(INVOKESTATIC, c_PistonEverything, "isBlockWhitelisted", String.format("(%s%sIII)Z", fieldDesc(c_Block), fieldDesc(c_World))));
						newInsns.add(new InsnNode(IRETURN));
						
						// throw out the rest of the tileentity check
						PistonEverything.logger.log(Level.TRACE, "removing TileEntity check");
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
	
	private byte[] transformBlockPistonMoving(String className, byte[] in)
    {
        ClassNode cNode = new ClassNode();
        ClassReader cr = new ClassReader(in);
        cr.accept(cNode, 0);
        
        MethodData m_setBlockBoundsBasedOnState = obfMapper.getMethodMapping("setBlockBoundsBasedOnState", "func_149719_a", "(Lnet/minecraft/world/IBlockAccess;III)V");
        MethodData m_hasTileEntity = obfMapper.getMethodMapping("hasTileEntity", "func_149716_u", "()Z"); //depreciated
        
        for (MethodNode mn : cNode.methods)
        {
            if (methodEquals(mn, m_setBlockBoundsBasedOnState))
            {
                PistonEverything.logger.log(Level.TRACE, "patching setBlockBoundsBasedOnState..");
                ListIterator<AbstractInsnNode> it = mn.instructions.iterator();
              
                InsnList newInsns = new InsnList();
                while (it.hasNext())
                {
                    AbstractInsnNode insn = it.next();
                    newInsns.add(insn);
                    
                    // don't calculate block bounds for te providers 
                    if (insn instanceof JumpInsnNode && insn.getOpcode() == IF_ACMPEQ)
                    {
                        //AbstractInsnNode prev = insn.getPrevious();
                        //if (prev != null && prev instanceof VarInsnNode && ((VarInsnNode)prev).var == 6)
                        //{
                        	PistonEverything.logger.log(Level.TRACE, "inserting tileentity check");
                            newInsns.add(new VarInsnNode(ALOAD, 6));
                            newInsns.add(new MethodInsnNode(INVOKEVIRTUAL, c_Block, m_hasTileEntity.name, m_hasTileEntity.desc));
                            newInsns.add(new JumpInsnNode(IFEQ, ((JumpInsnNode)insn).label));
                        //}
                    }
                }
                mn.instructions = newInsns;
            }
        }
        
        ClassWriter cw = new ClassWriter(COMPUTE_FRAMES);
        cNode.accept(cw);
        return cw.toByteArray();
    }
	
	private byte[] transformTileEntityRendererPiston(String className, byte[] in)
    {
        ClassNode cNode = new ClassNode();
        ClassReader cr = new ClassReader(in);
        cr.accept(cNode, 0);
        
        // class, field, and method mappings
        MethodData m_renderPiston = obfMapper.getMethodMapping("renderTileEntityAt", "func_147500_a", "(Lnet/minecraft/tileentity/TileEntityPiston;DDDF)V");
        MethodData m_renderBlockAllFaces = obfMapper.getMethodMapping("renderBlockAllFaces", "func_147769_a", "(Lnet/minecraft/block/Block;III)V");
        
        for (MethodNode mn : cNode.methods)
        {
            if (methodEquals(mn, m_renderPiston))
            {
                PistonEverything.logger.log(Level.TRACE, "patching renderTileEntityAt..");
                ListIterator<AbstractInsnNode> it = mn.instructions.iterator();
              
                InsnList newInsns = new InsnList();
                while (it.hasNext())
                {
                    AbstractInsnNode insn = it.next();
                    
                    if (insn instanceof MethodInsnNode && methodEquals(((MethodInsnNode) insn), m_renderBlockAllFaces))
                    {
                        PistonEverything.logger.log(Level.TRACE, "Replacing block render call");
                        newInsns.add(new VarInsnNode(FLOAD, 8));
                        newInsns.add(new VarInsnNode(ALOAD, 1));
                        insn = new MethodInsnNode(INVOKESTATIC, c_PistonEverything, "renderPistonBlock", String.format("(%s%sIIIF%s)V", fieldDesc(c_RenderBlocks), fieldDesc(c_Block), fieldDesc(c_TileEntityPiston)));
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
	
	@Override
	public byte[] transform(String arg0, String arg1, byte[] arg2)
	{
		//String className = remapper.map(arg0).replace('/','.');
		String className = arg1;
		if (className.equals("net.minecraft.tileentity.TileEntityPiston")) 
		{
			PistonEverything.logger.info("Patching class %s!", className);
			return transformTileEntityPiston(className, arg2);
		} 
		else if (className.equals("net.minecraft.block.BlockPistonBase"))
		{
			PistonEverything.logger.info("Patching class %s!", className);
			return transformBlockPistonBase(className, arg2);
		}
		else if (className.equals("net.minecraft.block.BlockPistonMoving"))
		{
		    PistonEverything.logger.info("Patching class %s!", className);
		    return transformBlockPistonMoving(className, arg2);
        }
		else if (className.equals("net.minecraft.client.renderer.tileentity.TileEntityRendererPiston"))
		{
		    PistonEverything.logger.info("Patching class %s!", className);
		    return transformTileEntityRendererPiston(className, arg2);
		}
		
		return arg2;
	}
}
