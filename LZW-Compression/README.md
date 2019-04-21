# LZW Compression
This project is based on LZW algorithm. It has three modes,
  * ```-n``` normal mode that still use the LZW codebook when it is full;
  * ```-r``` reset mode that resets the LZW codebook once it is full;
  * ```-m``` monitor mode that supervises the compression ratio. Once the ratio exceeds a specified amount, the LZW codebook will be reset.
