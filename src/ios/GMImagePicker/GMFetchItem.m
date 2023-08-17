//
//  GMFetchItem.m
//  GMPhotoPicker
//
//  Created by micheladrion on 4/26/15.
//  Copyright (c) 2015 Guillermo Muntaner Perelló. All rights reserved.
//

#import "GMFetchItem.h"

@implementation GMFetchItem

@synthesize be_progressed, be_finished, percent, image, be_saving_img;

- (id)init{
    
    self = [super init];
    
    be_progressed = false;
    be_finished = false;
    percent = 0;
//    image_thumb = nil;
//    image_fullsize = nil;
    image = nil;
    
    be_saving_img = false;
    //be_saving_img_thumb;
    
    return self;
}

@end
