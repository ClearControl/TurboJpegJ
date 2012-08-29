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

	private ByteBuffer mCompressedImageByteBuffer;
	private Pointer<Pointer<Byte>> mPointerToCompressedImageByteBufferPointer;
	private Pointer<CLong> mPointerToCompressedBufferEffectiveSize;
	private long mLastCompressionElapsedTimeInMs;

	private int mQuality = 100;

	public TurboJpegJCompressor()
	{
		super();
		mPointerToCompressor = TurbojpegLibrary.tjInitCompress();
	}

	@Override
	public void close() throws IOException
	{
		if (mPointerToCompressor == null)
			return;

		TurbojpegLibrary.tjDestroy(mPointerToCompressor);
		mPointerToCompressor = null;
		mPointerToCompressedImageByteBufferPointer = null;
		mPointerToCompressedBufferEffectiveSize.release();

	}

	public boolean compressMonochrome(final int pWidth, final int pHeight, final ByteBuffer p8BitImageByteBuffer)
	{
		if (mPointerToCompressor == null)
			return false;
		allocateCompressedBuffer(p8BitImageByteBuffer.limit());
		final StopWatch lCompressionTime = StopWatch.start();
		p8BitImageByteBuffer.position(0);
		final int lErrorCode = TurbojpegLibrary.tjCompress2(mPointerToCompressor,
																												Pointer.pointerToBytes(p8BitImageByteBuffer),
																												pWidth,
																												0,
																												pHeight,
																												(int) TJPF.TJPF_GRAY.value,
																												mPointerToCompressedImageByteBufferPointer,
																												mPointerToCompressedBufferEffectiveSize,
																												(int) TJSAMP.TJSAMP_GRAY.value,
																												mQuality,
																												TurbojpegLibrary.TJFLAG_NOREALLOC | TurbojpegLibrary.TJFLAG_FORCESSE3
																														| TurbojpegLibrary.TJFLAG_FASTDCT);
		mLastCompressionElapsedTimeInMs = lCompressionTime.time(TimeUnit.MILLISECONDS);
		mCompressedImageByteBuffer.limit((int) mPointerToCompressedBufferEffectiveSize.getCLong());
		return lErrorCode == 0;

	}

	private void allocateCompressedBuffer(final int pLength)
	{
		if (mCompressedImageByteBuffer != null && mCompressedImageByteBuffer.limit() == pLength)
			return;

		mCompressedImageByteBuffer = ByteBuffer.allocateDirect(pLength);

		if (mPointerToCompressedImageByteBufferPointer != null)
			mPointerToCompressedImageByteBufferPointer.release();
		mPointerToCompressedImageByteBufferPointer = Pointer.pointerToPointer(Pointer.pointerToBytes(mCompressedImageByteBuffer));

		if (mPointerToCompressedBufferEffectiveSize != null)
			mPointerToCompressedBufferEffectiveSize.release();
		mPointerToCompressedBufferEffectiveSize = Pointer.allocateCLong();
		mPointerToCompressedBufferEffectiveSize.setCLong(mCompressedImageByteBuffer.capacity());
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
