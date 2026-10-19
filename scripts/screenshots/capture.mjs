// Captures the screenshots used in the README from a running, demo-seeded stack:
//   docker compose up --build -d
//   cd scripts/screenshots && npm install && npm run capture
//
// BASE_URL   where the application listens           (default http://localhost:48080)
// CHROME_PATH path of a Chromium/Chrome binary; when unset, the browser managed by Playwright is used
//             (install it once with `npx playwright-core install chromium`)
import { chromium } from 'playwright-core';
import { fileURLToPath } from 'node:url';
import path from 'node:path';
import fs from 'node:fs';

const BASE = process.env.BASE_URL ?? 'http://localhost:48080';
const OUT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../docs/screenshots');
fs.mkdirSync(OUT, { recursive: true });

const browser = await chromium.launch({
  headless: true,
  executablePath: process.env.CHROME_PATH || undefined,
  args: ['--no-sandbox'],
});
const shot = async (page, name, opts = {}) => {
  await page.screenshot({ path: path.join(OUT, name), ...opts });
  console.log('captured', name);
};
const settle = (page, ms = 700) => page.waitForTimeout(ms);

// ------------------------------------------------------------------ customer front (phone-sized)
{
  const context = await browser.newContext({
    viewport: { width: 390, height: 780 },
    deviceScaleFactor: 2,
    isMobile: true,
    hasTouch: true,
  });
  const page = await context.newPage();

  await page.goto(`${BASE}/front/page/login.html`);
  await page.fill('input[placeholder*="手机号码"]', '13800138000');
  await page.click('text=获取验证码'); // dev profile echoes the code and fills it in
  await page.waitForFunction(() => document.querySelectorAll('input')[1]?.value?.length === 4);
  await settle(page);
  await shot(page, 'front-login.png');
  await page.click('button:has-text("登录")');
  await page.waitForURL('**/front/index.html');
  await page.waitForSelector('.divItem');
  await settle(page, 1200);

  // Signature Mains: a flavoured dish through the "choose options" dialog, plus plain ones
  await page.locator('.divItem', { hasText: 'Kung Pao Chicken' }).locator('.divTypes').click();
  await page.waitForSelector('.dialogFlavor .divContent span');
  await page.locator('.dialogFlavor .divContent span', { hasText: 'Hot' }).click();
  await settle(page, 400);
  await shot(page, 'front-flavor-dialog.png');
  await page.click('.dialogFlavor .divBottom >> text=加入购物车');
  await settle(page);
  const sweetSour = page.locator('.divItem', { hasText: 'Sweet and Sour Pork' });
  await sweetSour.locator('.divAdd img').click();
  await settle(page, 400);
  await page.locator('.divItem', { hasText: 'Beef with Broccoli' }).locator('.divAdd img').click();
  await settle(page, 400);
  await page.locator('.divItem', { hasText: 'Beef with Broccoli' }).locator('.divAdd img').click();
  await settle(page, 400);
  await page.locator('.divType li', { hasText: 'Noodles & Rice' }).click();
  await page.waitForSelector('.divItem:has-text("Yangzhou Fried Rice")');
  await page.locator('.divItem', { hasText: 'Yangzhou Fried Rice' }).locator('.divAdd img').click();
  await settle(page, 600);
  await page.locator('.divType li', { hasText: 'Signature Mains' }).click();
  await page.waitForSelector('.divItem:has-text("Kung Pao Chicken")');
  await settle(page, 900);
  await shot(page, 'front-menu.png');

  await page.click('.imgCartActive');
  await page.waitForSelector('.dialogCart .divCartItem');
  await settle(page, 800);
  await shot(page, 'front-cart.png');
  await page.keyboard.press('Escape');
  await page.mouse.click(195, 60); // click the overlay to close the cart sheet
  await settle(page);

  await page.goto(`${BASE}/front/page/add-order.html`);
  await page.waitForSelector('.itemList .item');
  await settle(page, 1200);
  await shot(page, 'front-checkout.png');
  await page.click('text=去支付');
  await page.waitForURL('**/pay-success.html');
  await settle(page);

  await page.goto(`${BASE}/front/page/order.html`);
  await page.waitForSelector('.item .dishList');
  await settle(page, 900);
  await shot(page, 'front-orders.png');
  await context.close();
}

// ------------------------------------------------------------------ admin console (desktop)
{
  const context = await browser.newContext({ viewport: { width: 1440, height: 860 } });
  const page = await context.newPage();

  await page.goto(`${BASE}/backend/page/login/login.html`);
  await settle(page, 900);
  await shot(page, 'admin-login.jpg', { type: 'jpeg', quality: 80 });
  await page.click('button:has-text("登录")');
  await page.waitForURL('**/backend/index.html');
  await settle(page, 900);

  const openMenu = async (label, rowSelector) => {
    await page.click(`.el-menu-item:has-text("${label}")`);
    const frame = page.frameLocator('iframe');
    await frame.locator(rowSelector).first().waitFor();
    await settle(page, 1000);
  };

  await openMenu('菜品管理', '.el-table__row');
  await shot(page, 'admin-dishes.png');
  await openMenu('套餐管理', '.el-table__row');
  await shot(page, 'admin-setmeals.png');
  await openMenu('订单明细', '.el-table__row');
  await shot(page, 'admin-orders.png');
  await context.close();
}

// ------------------------------------------------------------------ API documentation
{
  const context = await browser.newContext({ viewport: { width: 1440, height: 860 } });
  const page = await context.newPage();
  await page.goto(`${BASE}/doc.html`);
  await page.waitForSelector('text=onlineShopping', { timeout: 20000 }).catch(() => {});
  await settle(page, 2500);
  await shot(page, 'api-docs.png');
  await context.close();
}

await browser.close();
