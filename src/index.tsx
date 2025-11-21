import QrFromImage from './NativeQrFromImage';

export function scanFromPath(path: string): Promise<string[]> {
  return QrFromImage.scanFromPath(path);
}
