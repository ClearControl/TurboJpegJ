package turbojpegj.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

import org.bridj.CLong;
import org.bridj.Pointer;
import org.junit.Test;

import turbojpeg.TurbojpegLibrary;
import turbojpeg.TurbojpegLibrary.TJPF;
import turbojpeg.TurbojpegLibrary.TJSAMP;
import turbojpeg.utils.StopWatch;
import turbojpegj.TurboJpegJCompressor;

public class TurboJpegJCompressorTest
{

	@Test
	public void test() throws IOException
	{
		TurboJpegJCompressor lTurboJpegJCompressor = new TurboJpegJCompressor();
		lTurboJpegJCompressor.setQuality(90);
		ByteBuffer lByteBuffer = loadRawImage();
		//for (int i = 0; i < 100; i++)
			assertTrue(lTurboJpegJCompressor.compressMonochrome(549, 1080, lByteBuffer));
		int lLimit = lTurboJpegJCompressor.getCompressedBuffer().limit();
		final double lRatio = ((double)lLimit)/ lByteBuffer.limit();
		System.out.format("lRatio=%g \n",lRatio);
		assertTrue(lRatio<0.34);
	}

	private ByteBuffer loadRawImage() throws FileNotFoundException
	{
		try
		{
			final String lFileName = "dm.549x1080.8bit.raw";
			final URL resourceLocation = TurboJpegJCompressorTest.class.getResource(lFileName);
			if (resourceLocation == null)
			{
				throw new FileNotFoundException(lFileName);
			}
			final File myFile = new File(resourceLocation.toURI());
			final FileInputStream lFileInputStream = new FileInputStream(myFile);
			final FileChannel lChannel = lFileInputStream.getChannel();
			final ByteBuffer lByteBuffer = ByteBuffer.allocateDirect((int) lChannel.size());
			lChannel.read(lByteBuffer);
			lFileInputStream.close();
			return lByteBuffer;
		}
		catch (final URISyntaxException e)
		{
			e.printStackTrace();
		}
		catch (final IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
