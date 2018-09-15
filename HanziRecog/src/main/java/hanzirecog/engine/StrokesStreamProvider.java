package hanzirecog.engine;

import java.io.InputStream;

/**
 * A StrokesStreamProvider is something that can serve up InputStreams
 * to stroke recognizer formatted as expected by the matching algorithm (ie
 * a byte stream that was generated StrokesParser).
 */
public interface StrokesStreamProvider {

  /**
   * Get an InputStream instance streaming from the start of the same
   * stroke recognizer.  Each call to this method should return a new instance.
   *
   * @return stroke recognizer InputStream
   */
  public InputStream getStrokesStream();
}