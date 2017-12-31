#!/usr/bin/perl

use strict;
use warnings;

my @row0 = qw( 22 - 9 * );
my @row1 = qw( + 4 - 18 );
my @row2 = qw( 4 * 11 * );
my @row3 = qw( * 8 - 1 );

my @grid;
push @grid, \@row0;
push @grid, \@row1;
push @grid, \@row2;
push @grid, \@row3;


my $most_deep = 15;
my $found_pattern;

find_paths(0, 0, 0, []);

sub find_paths {
    my ($x, $y, $deep, $path) = @_;

    return if $deep > 15;

    my @steps = @{$path};

    if ($x == 3 && $y == 3) {

        push @steps, $grid[$x][$y];
        my $final_path = join ' ', @steps;

        my $total = shift @steps;
        while (scalar @steps) {
            my $op    = shift @steps;
            my $right = shift @steps;

            $total = eval( $total . $op . $right );
        }

        if ($total == 30) {
            if ($deep < $most_deep) {
                print $final_path, ' = ', $total, "\t", $deep, "\n";
                $most_deep = $deep;
                $found_pattern = $final_path;
            }

        }

        return;
    }

    if ($x > 0) {
        if ( !( $x - 1 == 0 && $y == 0 ) ) {
            my @next_steps = @steps;
            push @next_steps, $grid[$x][$y];
            find_paths($x - 1, $y, $deep + 1, \@next_steps);
        }
    }

    if ($x < 3) {
        my @next_steps = @steps;
        push @next_steps, $grid[$x][$y];
        find_paths($x + 1, $y, $deep + 1, \@next_steps);
    }

    if ($y > 0) {
        if ( !( $x == 0 && $y - 1 == 0 ) ) {
            my @next_steps = @steps;
            push @next_steps, $grid[$x][$y];
            find_paths($x, $y - 1, $deep + 1, \@next_steps);
        }
    }

    if ($y < 3) {
        my @next_steps = @steps;
        push @next_steps, $grid[$x][$y];
        find_paths($x, $y + 1, $deep + 1, \@next_steps);
    }
}
