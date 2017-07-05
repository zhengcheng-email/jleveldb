package com.tchaicatkovsky.jleveldb.test;

import org.junit.Test;

import com.tchaicatkovsky.jleveldb.RandomAccessFile0;
import com.tchaicatkovsky.jleveldb.SequentialFile;
import com.tchaicatkovsky.jleveldb.Status;
import com.tchaicatkovsky.jleveldb.WritableFile;
import com.tchaicatkovsky.jleveldb.util.ByteBuf;
import com.tchaicatkovsky.jleveldb.util.ByteBufFactory;
import com.tchaicatkovsky.jleveldb.util.UnpooledSlice;
import com.tchaicatkovsky.jleveldb.util.EnvImpl;
import com.tchaicatkovsky.jleveldb.util.FileUtils;
import com.tchaicatkovsky.jleveldb.util.Long0;
import com.tchaicatkovsky.jleveldb.util.Object0;
import com.tchaicatkovsky.jleveldb.util.Slice;

//import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestEnvImpl {
	@Test
	public void testWritableFile() {
		String dirname = "./data/test";
		EnvImpl env = new EnvImpl();
		FileUtils.deletePath("./data");
		
		env.createDir("./data");
		Status s = env.createDir(dirname);
		if (!s.ok()) {
			System.out.println(s.message());
		}
		assertTrue(s.ok());
		
		String fileName1 = dirname+"/testFile001";
		Object0<WritableFile> file0 = new Object0<WritableFile>();
		env.newWritableFile(fileName1, file0);
		String content = "123456";
		Slice s1 = new UnpooledSlice(content);
		s = file0.getValue().append(s1);
		assertTrue(s.ok());
		file0.getValue().close();
		file0.getValue().delete();
		file0.setValue(null);
		
		Long0 fileSize = new Long0();
		env.getFileSize(fileName1, fileSize);
		assertTrue(fileSize.getValue() == s1.size());
		
		ByteBuf buf = ByteBufFactory.newUnpooled();
		env.readFileToString(fileName1, buf);
		assertTrue((new UnpooledSlice(buf)).encodeToString().equals(content));
		
		
		String fileName2 = dirname+"/testFile002";
		env.writeStringToFile(new UnpooledSlice(content), fileName2);
		ByteBuf buf2 = ByteBufFactory.newUnpooled();
		env.readFileToString(fileName2, buf2);
		assertTrue((new UnpooledSlice(buf2)).encodeToString().equals(content));
		

		String fileName3 = dirname+"/testFile003";
		env.writeStringToFileSync(new UnpooledSlice(content), fileName3);
		ByteBuf buf3 = ByteBufFactory.newUnpooled();
		env.readFileToString(fileName3, buf3);
		assertTrue((new UnpooledSlice(buf3)).encodeToString().equals(content));
		
		String fileName4 = dirname+"/testFile004";
		env.renameFile(fileName3, fileName4);
		ByteBuf buf4 = ByteBufFactory.newUnpooled();
		env.readFileToString(fileName4, buf4);
		assertTrue((new UnpooledSlice(buf4)).encodeToString().equals(content));
		
		assertTrue(env.fileExists(fileName2));
		env.deleteFile(fileName2);
		assertTrue(!env.fileExists(fileName2));
		
		String appendContent = "abc";
		String newContent = content + appendContent;
		file0.setValue(null);
		env.newAppendableFile(fileName4, file0);
		file0.getValue().append(new UnpooledSlice(appendContent));
		file0.getValue().close();
		file0.getValue().delete();
		file0.setValue(null);
		ByteBuf buf5 = ByteBufFactory.newUnpooled();
		env.readFileToString(fileName4, buf5);
		assertTrue((new UnpooledSlice(buf5)).encodeToString().equals(newContent));
		
		Object0<RandomAccessFile0> file1 = new Object0<RandomAccessFile0>();
		env.newRandomAccessFile(fileName4, file1);
		Slice res = new UnpooledSlice();
		s = file1.getValue().read(5, 3, res , new byte[100]);
		System.out.println(s.message());
		assertTrue(res.encodeToString().equals(newContent.substring(5, 5+3)));
		file1.getValue().close();
		file1.getValue().delete();
		file1.setValue(null);
		
		
		res.clear();
		Object0<SequentialFile> file2 = new Object0<SequentialFile>();
		env.newSequentialFile(fileName4, file2);
		file2.getValue().skip(5);
		s = file2.getValue().read(3, res , new byte[100]);
		System.out.println(s.message());
		assertTrue(res.encodeToString().equals(newContent.substring(5, 5+3)));
		//file2.getValue().close();
		file2.getValue().delete();
		file2.setValue(null);
	}
	
//	@Test
//	public void testSchedule() throws Exception {
//		EnvImpl env = new EnvImpl();
//		for (int i = 1; i < 10; i++) {
//			env.schedule(new Runnable() {
//				@Override
//				public void run() {
//					System.out.println("=================>run success");
//				}
//			});
//			Thread.sleep(1000);
//		}
//		
//	}
}
