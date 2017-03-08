//
//  Footsteps.m
//  SimpleMap
//
//  Created by Rogers on 08/03/17.
//
//

#import "Footsteps.h"
@implementation Footsteps
-(void)setGoogleMapsViewController:(GoogleMapsViewController *)viewCtrl
{
    self.mapCtrl = viewCtrl;
}

/**
 * @param marker options
 * @return marker key
 */
-(void)createFootsteps:(CDVInvokedUrlCommand *)command
{
    NSDictionary *json = [command.arguments objectAtIndex:1];
    NSArray * markers = [[self attachSteps:json] copy];

    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:markers];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

-(NSMutableArray *)attachSteps:(NSDictionary *) json
{
    NSArray * positions = [json valueForKey:@"positions"];
    NSMutableArray * markers = [[NSMutableArray alloc] init];

    // Create icon
    NSMutableDictionary *iconProperty = nil;
    iconProperty = [json valueForKey:@"icon"];
    NSString * imageName = [self getImageName_:[iconProperty valueForKey:@"url"]];
    UIImage * image = [UIImage imageNamed:imageName];
    image = [image resize:18 height:37];


    for(NSDictionary * latLng in positions) {

        float latitude = [[latLng valueForKey:@"lat"] floatValue];
        float longitude = [[latLng valueForKey:@"lng"] floatValue];
        double heading = [[latLng valueForKey:@"rotation"] doubleValue];

        CLLocationCoordinate2D position = CLLocationCoordinate2DMake(latitude, longitude);
        GMSMarker *marker = [GMSMarker markerWithPosition:position];

        [marker setRotation:heading];

        if ([json valueForKey:@"flat"]) {
            [marker setFlat:[[json valueForKey:@"flat"] boolValue]];
        }

        if ([json valueForKey:@"opacity"]) {
            [marker setOpacity:[[json valueForKey:@"opacity"] floatValue]];
        }
        if ([json valueForKey:@"zIndex"]) {
            [marker setZIndex:[[json valueForKey:@"zIndex"] intValue]];
        }

        NSString *id = [NSString stringWithFormat:@"marker_%lu", (unsigned long)marker.hash];
        [self.mapCtrl.overlayManager setObject:marker forKey: id];

        // Custom properties
        NSMutableDictionary *properties = [[NSMutableDictionary alloc] init];
        NSString *markerPropertyId = [NSString stringWithFormat:@"marker_property_%lu", (unsigned long)marker.hash];

        [self.mapCtrl.overlayManager setObject:properties forKey: markerPropertyId];

        marker.icon = image;


        // Visible property
        if ([[json valueForKey:@"visible"] boolValue] == true) {
            iconProperty[@"visible"] = @YES;
        } else {
            iconProperty[@"visible"] = @NO;
        }


        NSMutableDictionary *result = [[NSMutableDictionary alloc] init];
        [result setObject:id forKey:@"id"];
        [result setObject:[NSString stringWithFormat:@"%lu", (unsigned long)marker.hash] forKey:@"hashCode"];


        if ([[json valueForKey:@"visible"] boolValue] == true) {
            marker.map = self.mapCtrl.map;
        }

        [markers addObject:result];
    }

    return markers;
}


/**
 * Remove the specified marker
 * @params MarkerKey
 */
-(void)remove:(CDVInvokedUrlCommand *)command
{
    NSString *markerKey = [command.arguments objectAtIndex:1];
    GMSMarker *marker = [self.mapCtrl.overlayManager objectForKey:markerKey];
    NSString *propertyId = [NSString stringWithFormat:@"marker_property_%lu", (unsigned long)marker.hash];
    marker.map = nil;
    [self.mapCtrl removeObjectForKey:markerKey];
    marker = nil;

    if ([self.mapCtrl.overlayManager objectForKey:propertyId]) {
        [self.mapCtrl removeObjectForKey:propertyId];
    }

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

/**
 * Set opacity
 * @params MarkerKey
 */
-(void)setOpacity:(CDVInvokedUrlCommand *)command
{
    NSString *markerKey = [command.arguments objectAtIndex:1];
    GMSMarker *marker = [self.mapCtrl.overlayManager objectForKey:markerKey];
    marker.opacity = [[command.arguments objectAtIndex:2] floatValue];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

/**
 * Set zIndex
 * @params MarkerKey
 */
-(void)setZIndex:(CDVInvokedUrlCommand *)command
{
    NSString *markerKey = [command.arguments objectAtIndex:1];
    GMSMarker *marker = [self.mapCtrl.overlayManager objectForKey:markerKey];
    marker.zIndex = [[command.arguments objectAtIndex:2] intValue];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

/**
 * Set visibility
 * @params MarkerKey
 */
-(void)setVisible:(CDVInvokedUrlCommand *)command
{
    NSString *markerKey = [command.arguments objectAtIndex:1];
    GMSMarker *marker = [self.mapCtrl.overlayManager objectForKey:markerKey];
    Boolean isVisible = [[command.arguments objectAtIndex:2] boolValue];

    if (isVisible) {
        marker.map = self.mapCtrl.map;
    } else {
        marker.map = nil;
    }

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


/**
 * Set flattable
 * @params MarkerKey
 */
-(void)setFlat:(CDVInvokedUrlCommand *)command
{
    NSString *markerKey = [command.arguments objectAtIndex:1];
    GMSMarker *marker = [self.mapCtrl.overlayManager objectForKey:markerKey];
    Boolean isFlat = [[command.arguments objectAtIndex:2] boolValue];
    [marker setFlat: isFlat];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

/**
 * set rotation
 */
-(void)setRotation:(CDVInvokedUrlCommand *)command
{
    NSString *markerKey = [command.arguments objectAtIndex:1];
    GMSMarker *marker = [self.mapCtrl.overlayManager objectForKey:markerKey];

    CLLocationDegrees degrees = [[command.arguments objectAtIndex:2] doubleValue];
    [marker setRotation:degrees];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}



/**
 * @private
 * Load the icon; then set to the marker
 */
-(NSString *)getImageName_:(NSString *) url {

    NSString * iconPath = url;

    NSRange range = [iconPath rangeOfString:@"http"];

    /**
     * Load the icon from local path
     */

    range = [iconPath rangeOfString:@"cdvfile://"];
    if (range.location != NSNotFound) {

        iconPath = [PluginUtil getAbsolutePathFromCDVFilePath:self.webView cdvFilePath:iconPath];

    }


    range = [iconPath rangeOfString:@"://"];
    if (range.location == NSNotFound) {
        range = [iconPath rangeOfString:@"www/"];
        if (range.location == NSNotFound) {
            iconPath = [NSString stringWithFormat:@"www/%@", iconPath];
        }

        range = [iconPath rangeOfString:@"/"];
        if (range.location != 0) {
            // Get the absolute path of the www folder.
            // https://github.com/apache/cordova-plugin-file/blob/1e2593f42455aa78d7fff7400a834beb37a0683c/src/ios/CDVFile.m#L506
            NSString *applicationDirectory = [[NSURL fileURLWithPath:[[NSBundle mainBundle] resourcePath]] absoluteString];
            iconPath = [NSString stringWithFormat:@"%@%@", applicationDirectory, iconPath];
        } else {
            iconPath = [NSString stringWithFormat:@"file://%@", iconPath];
        }
    }

    range = [iconPath rangeOfString:@"file://"];
    if (range.location != NSNotFound) {

#ifdef __CORDOVA_4_0_0
        NSURL *fileURL = [NSURL URLWithString:iconPath];
        NSURL *resolvedFileURL = [fileURL URLByResolvingSymlinksInPath];
        iconPath = [resolvedFileURL path];
#else
        iconPath = [iconPath stringByReplacingOccurrencesOfString:@"file://" withString:@""];
#endif

        NSFileManager *fileManager = [NSFileManager defaultManager];
        if (![fileManager fileExistsAtPath:iconPath]) {
            if (self.mapCtrl.debuggable) {
                NSLog(@"(debug)There is no file at '%@'.", iconPath);
            }
            return @"";
        }

    }

        return iconPath;

}


@end