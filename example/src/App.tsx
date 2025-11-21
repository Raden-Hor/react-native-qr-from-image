import { useState, useEffect } from 'react';
import { View, Text, Image, StyleSheet } from 'react-native';
import { scanFromPath } from 'react-native-qr-from-image';
import RNFS from 'react-native-fs';

const testQR = require('./assets/images/test-qr.png');

export default function App() {
  const [codes, setCodes] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    scanTestImage();
  }, []);

  const scanTestImage = async () => {
    try {
      setLoading(true);
      setError('');

      // Get the asset source
      const asset = Image.resolveAssetSource(testQR);

      // Download the asset to cache directory
      const destPath = `${RNFS.CachesDirectoryPath}/test-qr.png`;

      console.log('Downloading from:', asset?.uri);
      console.log('Saving to:', destPath);
      if (asset?.uri) {
        await RNFS.downloadFile({
          fromUrl: asset?.uri,
          toFile: destPath,
        }).promise;

        console.log('File saved, scanning...');

        // Now scan the local file
        const result = await scanFromPath(destPath);
        setCodes(result);
      }
    } catch (err) {
      console.error('Scan error:', err);
      setError(err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>QR Decoder Test</Text>
      <Image source={testQR} style={styles.image} />

      {loading && <Text>Scanning...</Text>}

      {error && <Text style={styles.error}>Error: {error}</Text>}

      {!loading && !error && codes.length > 0 && (
        <Text style={styles.result}>Result: {codes.join(', ')}</Text>
      )}

      {!loading && !error && codes.length === 0 && (
        <Text style={styles.noResult}>No codes found</Text>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
  },
  image: {
    width: 300,
    height: 300,
    marginBottom: 20,
    borderRadius: 8,
  },
  result: {
    fontSize: 16,
    textAlign: 'center',
  },
  error: {
    fontSize: 14,
    color: 'red',
    textAlign: 'center',
  },
  noResult: {
    fontSize: 16,
    color: '#999',
    fontStyle: 'italic',
  },
});
