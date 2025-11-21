# react-native-qr-from-image

Decode QR codes and barcodes from image files in React Native using ML Kit (Android) and Vision Framework (iOS) with ZXing fallback.

## Features

- 🚀 Fast barcode/QR code detection from images
- 📱 iOS (Vision Framework) and Android (ML Kit + ZXing) support
- 🔄 Automatic ZXing fallback on Android
- 💪 Turbo Module architecture
- 📦 Supports multiple barcode formats

## Installation
```sh
npm install react-native-qr-from-image
# or
yarn add react-native-qr-from-image
```

### iOS
```sh
cd ios && pod install
```

### Android

No additional steps needed. Gradle will handle dependencies automatically.

## Usage
```javascript
import { scanFromPath } from 'react-native-qr-from-image';

// Scan QR/Barcode from image path
const codes = await scanFromPath('file:///path/to/image.jpg');
console.log(codes); // ['QR_CODE_CONTENT', 'BARCODE_CONTENT']
```

### Complete Example
```javascript
import React, { useState } from 'react';
import { View, Button, Text, Image } from 'react-native';
import { scanFromPath } from 'react-native-qr-from-image';
import { launchImageLibrary } from 'react-native-image-picker';

export default function App() {
  const [codes, setCodes] = useState([]);
  const [imageUri, setImageUri] = useState('');

  const pickAndScan = async () => {
    const result = await launchImageLibrary({ mediaType: 'photo' });
    
    if (result.assets && result.assets[0].uri) {
      const uri = result.assets[0].uri;
      setImageUri(uri);
      
      const detectedCodes = await scanFromPath(uri);
      setCodes(detectedCodes);
    }
  };

  return (
    <View style={{ padding: 20 }}>
      <Button title="Pick Image & Scan" onPress={pickAndScan} />
      
      {imageUri && <Image source={{ uri: imageUri }} style={{ width: 200, height: 200 }} />}
      
      {codes.length > 0 && (
        <View>
          <Text>Detected codes:</Text>
          {codes.map((code, i) => (
            <Text key={i}>{code}</Text>
          ))}
        </View>
      )}
    </View>
  );
}
```

## Supported Barcode Formats

- QR Code
- Aztec
- Code 39
- Code 93
- Code 128
- Data Matrix
- EAN-8
- EAN-13
- ITF
- PDF417
- UPC-A
- UPC-E

## API

### `scanFromPath(path: string): Promise<string[]>`

Scans an image file and returns an array of detected barcode/QR code values.

**Parameters:**
- `path` (string): File path to the image (supports `file://` prefix)

**Returns:**
- `Promise<string[]>`: Array of detected code values

**Throws:**
- Error if image cannot be loaded or scanning fails

## Requirements

- React Native >= 0.70
- iOS >= 13.0
- Android >= API 21

## License

MIT

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository.

## Credits

- ML Kit for Android barcode scanning
- Vision Framework for iOS barcode scanning
- ZXing for fallback scanning on Android

MIT License

Copyright (c) 2025 Raden Hor

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.