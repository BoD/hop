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

import AppKit
import Foundation

@_cdecl("getAppIconPixels")
public func getAppIconPixels(
    _ path: UnsafePointer<CChar>,
    _ size: Int32,
    _ outData: UnsafeMutablePointer<UnsafeMutableRawPointer?>,
    _ outWidth: UnsafeMutablePointer<Int32>,
    _ outHeight: UnsafeMutablePointer<Int32>
) -> Int32 {
    let nsPath = String(cString: path)
    let icon = NSWorkspace.shared.icon(forFile: nsPath)

    let intSize = Int(size)
    guard let rep = NSBitmapImageRep(
        bitmapDataPlanes: nil,
        pixelsWide: intSize,
        pixelsHigh: intSize,
        bitsPerSample: 8,
        samplesPerPixel: 4,
        hasAlpha: true,
        isPlanar: false,
        colorSpaceName: .deviceRGB,
        bytesPerRow: intSize * 4,
        bitsPerPixel: 32
    ) else { return 0 }

    NSGraphicsContext.saveGraphicsState()
    NSGraphicsContext.current = NSGraphicsContext(bitmapImageRep: rep)
    icon.draw(in: NSRect(x: 0, y: 0, width: intSize, height: intSize))
    NSGraphicsContext.restoreGraphicsState()

    let byteCount = intSize * intSize * 4
    guard let buffer = malloc(byteCount), let bitmapData = rep.bitmapData else { return 0 }
    memcpy(buffer, bitmapData, byteCount)

    outData.pointee = buffer
    outWidth.pointee = size
    outHeight.pointee = size
    return 1
}

@_cdecl("getAllApplicationPaths")
public func getAllApplicationPaths() -> UnsafeMutablePointer<CChar>? {
    let semaphore = DispatchSemaphore(value: 0)
    var result: String?

    DispatchQueue.global().async {
        let queryThread = Thread {
            autoreleasepool {
                let query = NSMetadataQuery()
                query.predicate = NSPredicate(format: "kMDItemContentType == 'com.apple.application-bundle'")
                query.searchScopes = [NSMetadataQueryLocalComputerScope]

                NotificationCenter.default.addObserver(
                    forName: .NSMetadataQueryDidFinishGathering,
                    object: query,
                    queue: nil
                ) { [weak query] _ in
                    guard let query else { return }
                    query.stop()

                    var paths = Set<String>()

                    for item in query.results as? [NSMetadataItem] ?? [] {
                        if let path = item.value(forAttribute: NSMetadataItemPathKey) as? String {
                            paths.insert(path)
                        }
                    }

                    let knownDirs: [String] = [
                        "/Applications",
                        "/System/Applications",
                        "/System/Applications/Utilities",
                        "/System/Library/CoreServices/Applications",
                        (NSHomeDirectory() as NSString).appendingPathComponent("Applications"),
                    ]

                    let fm = FileManager.default
                    for dir in knownDirs {
                        guard let contents = try? fm.contentsOfDirectory(atPath: dir) else { continue }
                        for name in contents where name.hasSuffix(".app") {
                            paths.insert((dir as NSString).appendingPathComponent(name))
                        }
                    }

                    result = paths.joined(separator: "\n")
                    CFRunLoopStop(CFRunLoopGetCurrent())
                }

                _ = query.start()
                CFRunLoopRun()
                semaphore.signal()
            }
        }
        queryThread.start()
    }

    semaphore.wait()
    return strdup(result ?? "")
}

@_cdecl("freeBuffer")
public func freeBuffer(_ buffer: UnsafeMutableRawPointer?) {
    free(buffer)
}

@_cdecl("focusPreviousApp")
public func focusPreviousApp() {
    DispatchQueue.main.async {
        guard let prev = NSWorkspace.shared.menuBarOwningApplication else { return }
        // No clean public API exists post-macOS 14 for this; the deprecation is intentional
        prev.activate(options: .activateIgnoringOtherApps)
    }
}
