
// Registers a photo picker activity launcher in multi-select mode.
// In this example, the app lets the user select up to 5 media files.
ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
        registerForActivityResult(new PickMultipleVisualMedia(5), uris -> {
    // Callback is invoked after the user selects media items or closes the
    // photo picker.
    if (!uris.isEmpty()) {
        Log.d("PhotoPicker", "Number of items selected: " + uris.size());
    } else {
        Log.d("PhotoPicker", "No media selected");
    }
});

// For this example, launch the photo picker and let the user choose images
// and videos. If you want the user to select a specific type of media file,
// use the overloaded versions of launch(), as shown in the section about how
// to select a single media item.
pickMultipleMedia.launch(new PickVisualMediaRequest.Builder().setMediaType(PickVisualMedia.ImageAndVideo.INSTANCE).build());