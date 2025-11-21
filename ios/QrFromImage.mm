#import "QrFromImage.h"
#import <React/RCTLog.h>
#import <Vision/Vision.h>

@implementation QrFromImage

RCT_EXPORT_MODULE()


// TurboModule getter
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeQrFromImageSpecJSI>(params);
}

// MARK: - QR Code scanning
// Export the method for BOTH old and new architecture
RCT_EXPORT_METHOD(scanFromPath:(NSString *)path
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    if (!path || path.length == 0) {
        reject(@"INVALID_PATH", @"Path cannot be empty", nil);
        return;
    }
    
    // Handle different path formats
    NSURL *url;
    if ([path hasPrefix:@"file://"] || [path hasPrefix:@"http://"] || [path hasPrefix:@"https://"]) {
        url = [NSURL URLWithString:path];
    } else {
        url = [NSURL fileURLWithPath:path];
    }
    
    if (!url) {
        reject(@"INVALID_URL", @"Invalid image path", nil);
        return;
    }
    
    NSData *imageData = [NSData dataWithContentsOfURL:url];
    if (!imageData) {
        reject(@"FILE_ERROR", @"Cannot read image file", nil);
        return;
    }
    
    UIImage *image = [UIImage imageWithData:imageData];
    if (!image || !image.CGImage) {
        reject(@"IMAGE_ERROR", @"Cannot create image from data", nil);
        return;
    }

    CGImageRef cgImage = image.CGImage;

    // Create barcode detection request
    VNDetectBarcodesRequest *request = [[VNDetectBarcodesRequest alloc] initWithCompletionHandler:^(VNRequest * _Nonnull request, NSError * _Nullable error) {
        
        dispatch_async(dispatch_get_main_queue(), ^{
            if (error) {
                reject(@"VISION_ERROR", error.localizedDescription, error);
                return;
            }

            NSArray<VNBarcodeObservation *> *results = request.results;
            if (!results || results.count == 0) {
                reject(@"NO_QR_CODE", @"No QR codes found in image", nil);
                return;
            }

            NSMutableArray *qrCodes = [NSMutableArray array];
            for (VNBarcodeObservation *observation in results) {
                if (observation.payloadStringValue && observation.payloadStringValue.length > 0) {
                    [qrCodes addObject:observation.payloadStringValue];
                }
            }

            if (qrCodes.count == 0) {
                reject(@"NO_DATA", @"QR codes found but no readable data", nil);
            } else {
                resolve(qrCodes);
            }
        });
    }];

    // Only detect QR codes
    request.symbologies = @[VNBarcodeSymbologyQR];

#if TARGET_IPHONE_SIMULATOR
    request.revision = VNDetectBarcodesRequestRevision1;
#endif

    // Perform detection on background queue
    VNImageRequestHandler *handler = [[VNImageRequestHandler alloc] initWithCGImage:cgImage options:@{}];
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSError *handlerError;
        if (![handler performRequests:@[request] error:&handlerError]) {
            dispatch_async(dispatch_get_main_queue(), ^{
                reject(@"REQUEST_FAILED", handlerError.localizedDescription, handlerError);
            });
        }
    });
}

// Alternative method name that matches the spec interface
- (void)scanFromPath:(NSString *)path
       withResolver:(RCTPromiseResolveBlock)resolve
       withRejecter:(RCTPromiseRejectBlock)reject
{
    // Call the main implementation
    [self scanFromPath:path resolve:resolve reject:reject];
}

@end
