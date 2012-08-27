package turbojpegj;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.bridj.CLong;
import org.bridj.Pointer;

import turbojpeg.TurbojpegLibrary;
import turbojpeg.TurbojpegLibrary.TJPF;
import turbojpeg.TurbojpegLibrary.TJSAMP;
import turbojpeg.utils.StopWatch;

public class TurboJpegJCompressor implements Closeable
{

	private Pointer<?> mPointerToCompressor;

	private int mWidth;
	private int mHeight;
	private ByteBuffer mCompressedImageByteBuffer;
	private Pointer<Pointer<Byte>> mPointerToCompressedImageByteBufferPointer;
	private Pointer<CLong> mPointerToCompressedBufferEffectiveSize;
	private long mLastCompressionElapsedTimeInMs;

	private int mQuality=100;

	public TurboJpegJCompressor()
	{
		super();

	}

	public void initialize(final int pWidth, final int pHeight)
	{
		if (mPointerToCompressor != null && (pWidth != mWidth || pHeight != mHeight))
		{
			try
			{
				close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		mWidth = pWidth;
		mHeight = pHeight;

		mPointerToCompressor = TurbojpegLibrary.tjInitCompress();

		mCompressedImageByteBuffer = ByteBuffer.allocateDirect(pWidth * pHeight);

		mPointerToCompressedImageByteBufferPointer = Pointer.pointerToPointer(Pointer.pointerToBytes(mCompressedImageByteBuffer));

		mPointerToCompressedBufferEffectiveSize = Pointer.allocateCLong();
		mPointerToCompressedBufferEffectiveSize.setCLong(mCompressedImageByteBuffer.capacity());

	}

	@Override
	public void close() throws IOException
	{
		if(mPointerToCompressor==null) return;
		
		TurbojpegLibrary.tjDestroy(mPointerToCompressor);
		mPointerToCompressor=null;
		mPointerToCompressedImageByteBufferPointer=null;
		mPointerToCompressedBufferEffectiveSize.release();
		mWidth=0;
		mHeight=0;
	}
	
	

	public boolean compress(final ByteBuffer p8BitImageByteBuffer)
	{
		if (mPointerToCompressor == null)
			return false;

		final StopWatch lCompressionTime = StopWatch.start();
		final int lErrorCode = TurbojpegLibrary.tjCompress2(mPointerToCompressor,
																												Pointer.pointerToBytes(p8BitImageByteBuffer),
																												mWidth,
																												mWidth,
																												mHeight,
																												(int) TJPF.TJPF_GRAY.value,
																												mPointerToCompressedImageByteBufferPointer,
																												mPointerToCompressedBufferEffectiveSize,
																												(int) TJSAMP.TJSAMP_GRAY.value,
																												mQuality,
																												TurbojpegLibrary.TJFLAG_NOREALLOC | TurbojpegLibrary.TJFLAG_FORCESSE3
																														| TurbojpegLibrary.TJFLAG_FASTDCT);
		mLastCompressionElapsedTimeInMs = lCompressionTime.time(TimeUnit.MILLISECONDS);
		mCompressedImageByteBuffer.limit((int) mPointerToCompressedBufferEffectiveSize.getCLong());
		return lErrorCode > 0;

	}
	
	public ByteBuffer getCompressedBuffer()
	{
		return mCompressedImageByteBuffer;
	}
	
	public int getLastImageCompressionElapsedTimeInMs()
	{
		return (int) mLastCompressionElapsedTimeInMs;
	}

	public int getQuality()
	{
		return mQuality;
	}

	public void setQuality(int quality)
	{
		mQuality = quality;
	}

}
