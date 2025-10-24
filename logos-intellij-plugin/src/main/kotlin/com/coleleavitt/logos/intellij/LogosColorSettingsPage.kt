package com.coleleavitt.logos.intellij

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

/**
 * Color settings page for Logos language.
 *
 * This allows users to customize syntax highlighting colors in
 * Settings → Editor → Color Scheme → Logos
 */
class LogosColorSettingsPage : ColorSettingsPage {

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> {
        return DESCRIPTORS
    }

    override fun getColorDescriptors(): Array<ColorDescriptor> {
        return ColorDescriptor.EMPTY_ARRAY
    }

    override fun getDisplayName(): String {
        return "Logos"
    }

    override fun getIcon(): Icon {
        return LogosIcons.FILE
    }

    override fun getHighlighter(): SyntaxHighlighter {
        return LogosSyntaxHighlighter()
    }

    override fun getDemoText(): String {
        return """
// Logos Tweak Example
#import <UIKit/UIKit.h>

// Hook an existing class
%hook SpringBoard

- (void)applicationDidFinishLaunching:(id)application {
    %orig; // Call original implementation
    NSLog(@"SpringBoard loaded!");
}

%new
- (void)customMethod {
    Class cls = %c(MyClass);
    [cls performSelector:@selector(test)];
}

%end

// Create a runtime subclass
%subclass MyCustomView : UIView

%property (nonatomic, strong) NSString *customTitle;

- (instancetype)initWithFrame:(CGRect)frame {
    self = %orig;
    if (self) {
        self.customTitle = @"Hello";
    }
    return self;
}

%end

// Conditional initialization
%group iOS14
%hook UIViewController
- (void)viewDidLoad {
    %orig;
    %log; // Log all arguments
}
%end
%end group

// Constructor
%ctor {
    if (@available(iOS 14, *)) {
        %init(iOS14);
    } else {
        %init;
    }
}
        """.trimIndent()
    }

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? {
        return null
    }

}

private val DESCRIPTORS = arrayOf(
    AttributesDescriptor("Directive (Block)", LogosSyntaxHighlighter.LOGOS_DIRECTIVE),
    AttributesDescriptor("Directive (Special)", LogosSyntaxHighlighter.LOGOS_SPECIAL),
    AttributesDescriptor("Directive (Lifecycle)", LogosSyntaxHighlighter.LOGOS_LIFECYCLE),
    AttributesDescriptor("Directive (Advanced)", LogosSyntaxHighlighter.LOGOS_ADVANCED),
    AttributesDescriptor("Runtime Function", LogosSyntaxHighlighter.LOGOS_RUNTIME),
    AttributesDescriptor("Method Scope", LogosSyntaxHighlighter.OBJC_METHOD_SCOPE)
)