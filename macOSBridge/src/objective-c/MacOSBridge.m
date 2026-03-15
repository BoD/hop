// This source is part of the
//      _____  ___   ____
//  __ / / _ \/ _ | / __/___  _______ _
// / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
// \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
//                              /___/
// repository.
//
// Copyright (C) 2026-present Benoit 'BoD' Lubek (BoD@JRAF.org)
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

#import <AppKit/AppKit.h>
#import <CoreServices/CoreServices.h>

int getAppIconPixels(const char *path, int size, void **outData, int *outWidth, int *outHeight) {
    NSString *nsPath = [NSString stringWithUTF8String:path];
    NSImage *icon = [[NSWorkspace sharedWorkspace] iconForFile:nsPath];
    if (!icon) return 0;

    NSBitmapImageRep *rep = [[NSBitmapImageRep alloc]
            initWithBitmapDataPlanes:NULL
                          pixelsWide:size pixelsHigh:size
                       bitsPerSample:8 samplesPerPixel:4
                            hasAlpha:YES isPlanar:NO
                      colorSpaceName:NSDeviceRGBColorSpace
                         bytesPerRow:size * 4
                        bitsPerPixel:32];

    NSGraphicsContext *ctx = [NSGraphicsContext graphicsContextWithBitmapImageRep:rep];
    [NSGraphicsContext saveGraphicsState];
    [NSGraphicsContext setCurrentContext:ctx];
    [icon drawInRect:NSMakeRect(0, 0, size, size)];
    [NSGraphicsContext restoreGraphicsState];

    int byteCount = size * size * 4;
    void *buffer = malloc(byteCount);
    memcpy(buffer, [rep bitmapData], byteCount);
    *outData = buffer;
    *outWidth = size;
    *outHeight = size;
    return 1;
}

static void scanDir(NSString *dir, NSMutableSet *results) {
    NSFileManager *fm = [NSFileManager defaultManager];
    NSArray *contents = [fm contentsOfDirectoryAtPath:dir error:nil];
    for (NSString *name in contents) {
        if ([name hasSuffix:@".app"]) {
            [results addObject:[dir stringByAppendingPathComponent:name]];
        }
    }
}

const char *getAllApplicationPaths() {
    __block NSString *result = nil;
    dispatch_semaphore_t sem = dispatch_semaphore_create(0);

    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        @autoreleasepool {
            NSThread *queryThread = [[NSThread alloc] initWithBlock:^{
                @autoreleasepool {
                    NSMetadataQuery *query = [[NSMetadataQuery alloc] init];
                    query.predicate = [NSPredicate predicateWithFormat:
                            @"kMDItemContentType == 'com.apple.application-bundle'"];
                    query.searchScopes = @[NSMetadataQueryLocalComputerScope];

                    [[NSNotificationCenter defaultCenter]
                            addObserverForName:NSMetadataQueryDidFinishGatheringNotification
                                        object:query queue:nil
                            usingBlock:^(NSNotification *note) {
                                [query stopQuery];

                                NSMutableSet *paths = [NSMutableSet set];

                                // Add Spotlight results
                                for (NSMetadataItem *item in query.results) {
                                    NSString *path = [item valueForAttribute:NSMetadataItemPathKey];
                                            if (path) [paths addObject:path];
                                        }

                                // Overlay direct scans of known dirs
                                NSArray *knownDirs = @[
                                        @"/Applications",
                                        @"/System/Applications",
                                        @"/System/Applications/Utilities",
                                        @"/System/Library/CoreServices/Applications",
                                        [NSHomeDirectory() stringByAppendingPathComponent:@"Applications"],
                                ];
                                for (NSString *dir in knownDirs) {
                                    NSArray *contents = [[NSFileManager defaultManager]
                                            contentsOfDirectoryAtPath:dir error:nil];
                                    for (NSString *name in contents) {
                                        if ([name hasSuffix:@".app"]) {
                                            [paths addObject:[dir stringByAppendingPathComponent:name]];
                                        }
                                    }
                                }

                                result = [[[paths allObjects] componentsJoinedByString:@"\n"] retain];

                                        CFRunLoopStop(CFRunLoopGetCurrent());
                                    }];

                    [query startQuery];
                    CFRunLoopRun();

                    [query release];
                    dispatch_semaphore_signal(sem);
                }
            }];
            [queryThread start];
        }
    });

    dispatch_semaphore_wait(sem, DISPATCH_TIME_FOREVER);
    dispatch_release(sem);

    if (!result) return strdup("");

    const char *cstr = strdup(result.UTF8String);
    [result release];
    return cstr;
}

void freeBuffer(void *buffer) {
    free(buffer);
}

void focusPreviousApp() {
    dispatch_async(dispatch_get_main_queue(), ^{
        @autoreleasepool {
            NSRunningApplication *prev = [[NSWorkspace sharedWorkspace] menuBarOwningApplication];
            // No clean public API exists post-macOS 14 for this; pragma is intentional
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
            [prev activateWithOptions:NSApplicationActivateIgnoringOtherApps];
#pragma clang diagnostic pop
        }
    });
}
