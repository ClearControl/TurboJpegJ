package turbojpeg.test;

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

public class LibJpegTurboTests
{

	@Test
	public void testDMImage() throws IOException
	{
		final ByteBuffer lDmImage = loadRawImage();
		final ByteBuffer lCompressedImage = ByteBuffer.allocateDirect(512 * 1024);

		final Pointer<Pointer<Byte>> lPointerToPointer = Pointer.pointerToPointer(Pointer.pointerToBytes(lCompressedImage));

		final Pointer<?> lPointerToCompressor = TurbojpegLibrary.tjInitCompress();

		final Pointer<CLong> lPointerToCLong = Pointer.allocateCLong();

		lPointerToCLong.setCLong(lCompressedImage.capacity());

		final StopWatch lCompressionTime = StopWatch.start();
		final int ec = TurbojpegLibrary.tjCompress2(lPointerToCompressor,
																								Pointer.pointerToBytes(lDmImage),
																								512,
																								512,
																								1024,
																								(int) TJPF.TJPF_GRAY.value,
																								lPointerToPointer,
																								lPointerToCLong,
																								(int) TJSAMP.TJSAMP_GRAY.value,
																								100,
																								TurbojpegLibrary.TJFLAG_NOREALLOC | TurbojpegLibrary.TJFLAG_ACCURATEDCT);
		final long lCompressionElapsedTime = lCompressionTime.time(TimeUnit.MILLISECONDS);
		System.out.format("Compression: %d ms \n",
											lCompressionElapsedTime);

		System.out.println(ec);

		lCompressedImage.limit((int) lPointerToCLong.getCLong());

		final double ratio = (double) lCompressedImage.limit() / lDmImage.limit();

		System.out.format("Compression ratio: %g  \n", ratio);

		final ByteBuffer lDmImageDecompressed = ByteBuffer.allocateDirect(lDmImage.limit());

		final Pointer<?> lPointerToDecompressor = TurbojpegLibrary.tjInitDecompress();

		final StopWatch lDecompressionTime = StopWatch.start();
		final int ed = TurbojpegLibrary.tjDecompress2(lPointerToDecompressor,
																									Pointer.pointerToBytes(lCompressedImage),
																									lCompressedImage.limit(),
																									Pointer.pointerToBytes(lDmImageDecompressed),
																									512,
																									512,
																									1024,
																									(int) TJPF.TJPF_GRAY.value,
																									TurbojpegLibrary.TJFLAG_ACCURATEDCT);
		final long lDecompressionElapsedTime = lDecompressionTime.time(TimeUnit.MILLISECONDS);
		System.out.format("Decompression: %d ms \n",
											lDecompressionElapsedTime);

		System.out.println(ed);

		int counter = 0;
		for (int i = 0; i < lDmImage.limit(); i++)
		{
			final byte a = lDmImageDecompressed.get(i);
			final byte b = lDmImage.get(i);
			if (a != b)
			{
				// System.out.format(" %d != %d \n", a, b);
				counter++;
			}

		}
		System.out.format("number of differences: %d \n", counter);

		final FileOutputStream lFileOutputStream = new FileOutputStream("out.raw");
		final FileChannel lChannel = lFileOutputStream.getChannel();
		lChannel.write(lDmImageDecompressed);
		lChannel.force(false);
		lChannel.close();
	}

	private ByteBuffer loadRawImage() throws FileNotFoundException
	{
		try
		{
			final String lFileName = "dm.512x1024.8bit.raw";
			final URL resourceLocation = LibJpegTurboTests.class.getResource(lFileName);
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
