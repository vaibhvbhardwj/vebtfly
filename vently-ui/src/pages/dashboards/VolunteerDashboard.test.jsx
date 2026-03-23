/**
 * VolunteerDashboard Component Tests
 * 
 * This file documents the test cases for the VolunteerDashboard component.
 * To run these tests, install a testing framework like Vitest or Jest.
 * 
 * Test Framework: Vitest (recommended) or Jest
 * Testing Library: React Testing Library
 * 
 * Installation:
 * npm install -D vitest @testing-library/react @testing-library/jest-dom
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import VolunteerDashboard from './VolunteerDashboard';

// Mock the useAuth hook
vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => ({
    user: {
      id: 1,
      name: 'John Doe',
      role: 'VOLUNTEER',
      email: 'john@example.com',
    },
    isAuthenticated: true,
    token: 'mock-token',
  }),
}));

describe('VolunteerDashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Stat Calculations', () => {
    it('should display correct active applications count', async () => {
      render(
        <BrowserRouter>
          <VolunteerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        const activeAppsElement = screen.getByText('Active Applications').closest('div');
        expect(activeAppsElement).toBeInTheDocument();
        expect(activeAppsElement.textContent).toContain('3');
      });
    });

    it('should display correct upcoming events count', async () => {
      render(
        <BrowserRouter>
          <VolunteerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        const upcomingElement = screen.getByText('Upcoming Events').closest('div');
        expect(upcomingElement).toBeInTheDocument();
        expect(upcomingElement.textContent).toContain('2');
      });
    });

    it('should display total earnings correctly', async () => {
      render(
        <BrowserRouter>
          <VolunteerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        const earningsElement = screen.getByText('Total Earnings').closest('div');
        expect(earningsElement).toBeInTheDocument();
        expect(earningsElement.textContent).toContain('5,400');
      });
    });

    it('should display average rating correctly', async () => {
      render(
        <BrowserRouter>
          <VolunteerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        const ratingElement = screen.getByText('Your Rating').closest('div');
        expect(ratingElement).toBeInTheDocument();
        expect(ratingElement.textContent).toContain('4.8');
      });
    });
  });

  describe('Quick Action Links', () => {
    it('should render Browse Events link', async () => {
      render(
        <BrowserRouter>
          <VolunteerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        const browseLink = screen.getByText('Browse Events');
        expect(browseLink).toBeInTheDocument();
        expect(browseLink.closest('a')).toHaveAttribute('href', '/events');
      });
    });

    it('should render My Applications link', async () => {
      render(
        <BrowserRouter>
          <VolunteerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        const applicationsLink = screen.getByText('My Applications');
        expect(applicationsLink).toBeInTheDocument();
        expect(applicationsLink.closest('a')).toHaveAttribute('href', '/applications');
      });
    });

    it('should render View All link for Recommended Events', async () => {
      render(
        <BrowserRouter>
          <VolunteerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        const viewAllLinks = screen.getAllByText(/View All/);
        expect(viewAllLinks.length).toBeGreaterThan(0);
      });
    });
  });

  describe('Dashboard Sections', () => {
    it('should display welcome message with user name', async () => {
      render(
        <BrowserRouter>
          <VolunteerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        expect(screen.getByText(/Welcome back, John Doe/)).toBeInTheDocument();
      });
    });

    it('should display Recommended Events section', async () => {
      render(
        <BrowserRouter>
          <VolunteerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        expect(screen.getByText(/Recommended Events/)).toBeInTheDocument();
      });
    });

    it('should display My Applications summary', async () => {
      render(
        <BrowserRouter>
          <VolunteerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        expect(screen.getByText('My Applications')).toBeInTheDocument();
      });
    });

    it('should display Pending Actions section', async () => {
      render(
        <BrowserRouter>
          <VolunteerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        expect(screen.getByText(/Pending Actions/)).toBeInTheDocument();
      });
    });

    it('should display Earnings Trend chart', async () => {
      render(
        <BrowserRouter>
          <VolunteerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        expect(screen.getByText('Earnings Trend')).toBeInTheDocument();
      });
    });

    it('should display Application Status pie chart', async () => {
      render(
        <BrowserRouter>
          <VolunteerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        expect(screen.getByText('Application Status')).toBeInTheDocument();
      });
    });
  });

  describe('Loading State', () => {
    it('should display loading spinner while fetching data', () => {
      render(
        <BrowserRouter>
          <VolunteerDashboard />
        </BrowserRouter>
      );

      // The loading state should be brief, but we can verify the component renders
      expect(screen.getByText(/Loading your dashboard/i)).toBeInTheDocument();
    });
  });

  describe('Responsive Design', () => {
    it('should render stats grid with responsive columns', async () => {
      render(
        <BrowserRouter>
          <VolunteerDashboard />
        </BrowserRouter>
      );

      await waitFor(() => {
        const statsGrid = screen.getByText('Active Applications').closest('div').closest('div').closest('div');
        expect(statsGrid).toHaveClass('grid');
        expect(statsGrid).toHaveClass('grid-cols-1');
        expect(statsGrid).toHaveClass('md:grid-cols-2');
        expect(statsGrid).toHaveClass('lg:grid-cols-4');
      });
    });
  });
});
