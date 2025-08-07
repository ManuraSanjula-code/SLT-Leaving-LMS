/** @type {import('next').NextConfig} */
const nextConfig = {
    trailingSlash: false,
    images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'media.giphy.com',
        port: '',
        pathname: '/**',
      },
    ],
  },
};

export default nextConfig;
